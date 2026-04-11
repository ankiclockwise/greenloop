package com.greenloop.auth;

/**
 * Exception thrown when a user exceeds the rate limit for an operation
 * such as resending verification emails or attempting logins.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
