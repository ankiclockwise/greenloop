package com.greenloop.auth;

/**
 * Exception thrown when an email verification token cannot be found
 * in the database.
 */
public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException(String message) {
        super(message);
    }

    public TokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
