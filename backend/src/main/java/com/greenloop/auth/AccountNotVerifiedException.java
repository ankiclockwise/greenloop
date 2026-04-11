package com.greenloop.auth;

/**
 * Exception thrown when a user attempts to access protected resources
 * without verifying their email address.
 */
public class AccountNotVerifiedException extends RuntimeException {

    public AccountNotVerifiedException(String message) {
        super(message);
    }

    public AccountNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
