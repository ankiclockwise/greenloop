package com.greenloop.auth;

/**
 * Exception thrown when attempting to use an email verification token
 * that has already been used for verification.
 */
public class TokenAlreadyUsedException extends RuntimeException {

    public TokenAlreadyUsedException(String message) {
        super(message);
    }

    public TokenAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
