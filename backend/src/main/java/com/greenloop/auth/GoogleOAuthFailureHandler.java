package com.greenloop.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuthFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Extract error information
        String errorMessage = exception.getMessage();
        String errorCode = "oauth_failed";

        // Map specific exception types to error codes
        if (exception.getCause() != null) {
            String causeName = exception.getCause().getClass().getSimpleName();
            if (causeName.contains("InvalidClient")) {
                errorCode = "invalid_client";
                errorMessage = "OAuth configuration error. Please contact support.";
            } else if (causeName.contains("InvalidGrant")) {
                errorCode = "invalid_grant";
                errorMessage = "Authorization code is invalid or expired.";
            } else if (causeName.contains("InvalidScope")) {
                errorCode = "invalid_scope";
                errorMessage = "Requested scopes are invalid.";
            } else if (causeName.contains("ServerError")) {
                errorCode = "server_error";
                errorMessage = "OAuth server encountered an error. Please try again.";
            } else if (causeName.contains("TemporarilyUnavailable")) {
                errorCode = "temporarily_unavailable";
                errorMessage = "OAuth server is temporarily unavailable. Please try again later.";
            }
        }

        // If no specific error message, use a generic one
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Google authentication failed. Please try again.";
        }

        // Encode error message for URL parameter
        String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        // Build redirect URL to frontend login page with error
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/login")
                .queryParam("error", errorCode)
                .queryParam("message", encodedErrorMessage)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
