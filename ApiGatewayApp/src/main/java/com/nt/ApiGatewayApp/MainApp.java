package com.nt.ApiGatewayApp;

import io.vertx.core.Vertx;

/**
 * Main application to launch the Vert.x API Gateway.
 */
public class MainApp {
    public static void main(String[] args) {
        Vertx vertx=Vertx.vertx();
        vertx.deployVerticle(new ApiGatewayVerticle());
    }
}
