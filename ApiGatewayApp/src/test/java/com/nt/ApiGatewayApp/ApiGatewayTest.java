package com.nt.ApiGatewayApp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

/**
 * Unit test for the /aggregate endpoint of the Vert.x API Gateway. Uses JUnit
 * 5.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiGatewayTest {

	private static Vertx vertx;

	/**
	 * Initializes Vert.x and deploys the API Gateway Verticle before all tests.
	 */

	@BeforeAll
	public static void setup() {
		vertx = Vertx.vertx();
		vertx.deployVerticle(new ApiGatewayVerticle());
	}

	/**
	 * Tests the /aggregate endpoint to ensure it returns valid JSON containing
	 * post_title and author_name.
	 *
	 * NOTE: Because Vert.x uses asynchronous operations, we wait briefly using
	 * Thread.sleep(). This is a simple approach; for more advanced async test
	 * control, consider using VertxUnit or CountDownLatch.
	 */
	@Test
	@Order(1)
	public void testAggregateEndpoint() throws InterruptedException {
		WebClient client = WebClient.create(vertx);
		client.get(8080, "localhost", "/aggregate").send(ar -> {
			assertTrue(ar.succeeded(), "Request should succeed");
			JsonObject response = ar.result().bodyAsJsonObject();
			// Ensure that the keys are present in the response
			assertNotNull(response.getString("post_title"));
			assertNotNull(response.getString("author_name"));
			// Optional: Print response for debugging

			System.out.println("Response" + response.encodePrettily());
		});
		// Wait for async response (quick fix for test timing)
		Thread.sleep(2000);
	}

	/**
	 * Shuts down Vert.x after all tests complete.
	 */
	@AfterAll
	public static void tearDown() {
		vertx.close();
	}
}
