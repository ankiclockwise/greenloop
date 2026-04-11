package com.greenloop.auth;

/**
 * Exception thrown when attempting to create a user account that already exists.
 * Typically thrown when an email address is already registered in the system.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
