package com.nt.ApiGatewayApp;



/**
 * Centralized constants for the API Gateway application.
 */
public class AppConstants {
	
	
	// API Gateway Port
    public static final int SERVER_PORT = 8080;
    
    // API Gateway Status code
    public static final int STATUS_CODE = 500;

    // External API URLs
    public static final String POSTS_API_URL = "https://jsonplaceholder.typicode.com/posts/1";
    public static final String USERS_API_URL = "https://jsonplaceholder.typicode.com/users/1";

    // HTTP Headers
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    // JSON Keys
    public static final String POST_TITLE_KEY = "post_title";
    public static final String AUTHOR_NAME_KEY = "author_name";
    public static final String ERROR_KEY = "error";

    // Fallback values
    public static final String FALLBACK_POST_TITLE = "Unavailable";
    public static final String FALLBACK_AUTHOR_NAME = "Unknown";
    public static final String FALLBACK_ERROR_MESSAGE = "One or more API calls failed";

    // Circuit Breaker Config
    public static final int MAX_FAILURES = 3;
    public static final int TIMEOUT_MS = 2000;
    public static final int RESET_TIMEOUT_MS = 5000;

}
