package com.nt.ApiGatewayApp;

import java.util.List;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * API Gateway Verticle using Vert.x with Circuit Breaker. It listens on
 * /aggregate and fetches data from two external APIs concurrently.
 */
public class ApiGatewayVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiGatewayVerticle.class);

	public void start(Promise<Void> startPromise) {
		// Create a WebClient instance for making HTTP requests
		WebClient client = WebClient.create(vertx);
		// Configure Circuit Breaker
		CircuitBreaker breaker = CircuitBreaker.create("api-circuit-breaker", vertx,
				new CircuitBreakerOptions().setMaxFailures(3)// Open circuit after 3 consecutive failures
						.setTimeout(2000) // Timeout for each call (2 seconds)
						.setFallbackOnFailure(true) // Use fallback on failure
						.setResetTimeout(5000)); // Retry service after 5 seconds

		// Create a router for handling HTTP requests
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		// Define /aggregate endpoint
		router.get("/aggregate").handler(ctx -> {
			LOGGER.info("Received request: /aggregate");
			// Call to fetch post title
			Future<JsonObject> postFuture = breaker.executeWithFallback(promise -> {
				LOGGER.info("Calling posts API...");
				client.getAbs("https://jsonplaceholder.typicode.com/posts/1").send(ar -> {
					if (ar.succeeded()) {
						// Extract title from response
						String title = ar.result().bodyAsJsonObject().getString("title");
						LOGGER.info("Post title fetched: " + title);
						promise.complete(new JsonObject().put("post_title", title));
					} else {
						LOGGER.error("Failed to fetch post data", ar.cause());
						promise.fail("Post API failed");
					}
				});
			}, v -> {
				LOGGER.warn("Post API fallback triggered.");
				return new JsonObject().put("post_title", "Unavailable");
			}); // Fallback if API fails

			// Call to fetch user name
			Future<JsonObject> userFuture = breaker.executeWithFallback(promise -> {
				LOGGER.info("Calling users API...");
				client.getAbs("https://jsonplaceholder.typicode.com/users/1").send(ar -> {
					if (ar.succeeded()) {
						// Extract name from response
						String name = ar.result().bodyAsJsonObject().getString("name");
						LOGGER.info("User name fetched: " + name);
						promise.complete(new JsonObject().put("author_name", name));
					} else {
						LOGGER.error("Failed to fetch user data", ar.cause());
						promise.fail("User API failed");
					}
				});
			}, v -> {
				LOGGER.warn("User API fallback triggered.");
				return new JsonObject().put("author_name", "Unknown"); // Fallback if API fails
			});

			// Combine both futures
			CompositeFuture.all(postFuture, userFuture).onComplete(ar -> {
				if (ar.succeeded()) {
					JsonObject result = postFuture.result().mergeIn(userFuture.result());
					LOGGER.info("Sending successful aggregated response.");
					ctx.response().putHeader("Content-Type", "application/json").end(result.encodePrettily());
				} else {
					LOGGER.error("One or more API calls failed.", ar.cause());
					ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json")
							.end(new JsonObject().put("error", "One or more API calls failed").encodePrettily());
				}
			});

		});

		// Start HTTP server on port 8080
		vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
			if (http.succeeded()) {
				LOGGER.info("HTTP server started on port 8080");
				startPromise.complete();
				// System.out.println("HTTP server started on port 8080");
			} else {
				LOGGER.error("Failed to start HTTP server", http.cause());
				startPromise.fail(http.cause());
			}
		});
	}

}
