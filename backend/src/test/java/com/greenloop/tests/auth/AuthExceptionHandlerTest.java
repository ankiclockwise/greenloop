package com.greenloop.tests.auth;

import com.greenloop.auth.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthExceptionHandlerTest {

    @Mock private WebRequest request;

    private AuthExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new AuthExceptionHandler();
        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handleBadCredentials_returns401() {
        ResponseEntity<?> response = handler.handleBadCredentials(
                new BadCredentialsException("bad creds"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleAccountNotVerified_returns403() {
        ResponseEntity<?> response = handler.handleAccountNotVerified(
                new AccountNotVerifiedException("not verified"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleRateLimit_returns429() {
        ResponseEntity<?> response = handler.handleRateLimit(
                new RateLimitExceededException("rate limit"), request);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    }

    @Test
    void handleUserAlreadyExists_returns409() {
        ResponseEntity<?> response = handler.handleUserAlreadyExists(
                new UserAlreadyExistsException("exists"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleTokenExpired_returns400() {
        ResponseEntity<?> response = handler.handleTokenExpired(
                new TokenExpiredException("expired"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleTokenAlreadyUsed_returns400() {
        ResponseEntity<?> response = handler.handleTokenAlreadyUsed(
                new TokenAlreadyUsedException("used"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleTokenNotFound_returns400() {
        ResponseEntity<?> response = handler.handleTokenNotFound(
                new TokenNotFoundException("not found"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleInvalidEmail_returns400() {
        ResponseEntity<?> response = handler.handleInvalidEmail(
                new InvalidEmailException("invalid"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleEmailAlreadyVerified_returns409() {
        ResponseEntity<?> response = handler.handleEmailAlreadyVerified(
                new EmailAlreadyVerifiedException("already verified"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleUserNotFound_returns404() {
        ResponseEntity<?> response = handler.handleUserNotFound(
                new UserNotFoundException("not found"), request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleEmailSending_returns500() {
        ResponseEntity<?> response = handler.handleEmailSending(
                new EmailSendingException("failed"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleGlobal_returns500() {
        ResponseEntity<?> response = handler.handleGlobal(
                new RuntimeException("unexpected"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
