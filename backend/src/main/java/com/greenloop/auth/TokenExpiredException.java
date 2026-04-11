package com.greenloop.auth;

/**
 * Exception thrown when an email verification token has expired
 * and can no longer be used for verification.
 */
public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
