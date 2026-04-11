package com.greenloop.auth;

/**
 * Exception thrown when an email address fails validation.
 * Typically occurs when a non-university (.edu) email is used for GreenLoop registration.
 */
public class InvalidEmailException extends RuntimeException {

    public InvalidEmailException(String message) {
        super(message);
    }

    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
