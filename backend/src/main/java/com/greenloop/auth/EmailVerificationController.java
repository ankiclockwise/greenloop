package com.greenloop.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST Controller for handling email verification endpoints.
 * Provides endpoints for verifying email tokens and resending verification emails.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    /**
     * Verifies an email address using a verification token.
     * Token is provided in the request body.
     *
     * @param request the verification request containing the token
     * @return ResponseEntity with verification result
     */
    @PostMapping("/verify-email")
    public ResponseEntity<VerificationResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification request received");

        try {
            verificationService.verifyToken(request.getToken());

            VerificationResponse response = VerificationResponse.builder()
                    .success(true)
                    .message("Email verified successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (TokenExpiredException | TokenAlreadyUsedException | TokenNotFoundException e) {
            log.error("Token verification failed: {}", e.getMessage());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Resends a verification email to the specified email address.
     * Rate-limited to a maximum of 3 resends per hour.
     *
     * @param request the resend request containing the email address
     * @return ResponseEntity with resend result
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<VerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification request received for email: {}", request.getEmail());

        try {
            verificationService.resendVerification(request.getEmail());

            VerificationResponse response = VerificationResponse.builder()
                    .success(true)
                    .message("Verification email sent successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (EmailAlreadyVerifiedException e) {
            log.warn("Resend verification attempted for already verified email: {}", request.getEmail());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for resend verification: {}", request.getEmail());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);

        } catch (UserNotFoundException e) {
            log.error("User not found for resend verification: {}", request.getEmail());
            VerificationResponse response = VerificationResponse.builder()
                    .success(false)
                    .message("No account found with this email address")
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * DTO for email verification request.
     */
    public static class VerifyEmailRequest {
        @NotBlank(message = "Verification token is required")
        private String token;

        public VerifyEmailRequest() {}

        public VerifyEmailRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * DTO for resend verification request.
     */
    public static class ResendVerificationRequest {
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        private String email;

        public ResendVerificationRequest() {}

        public ResendVerificationRequest(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * DTO for verification response.
     */
    public static class VerificationResponse {
        private boolean success;
        private String message;

        public VerificationResponse() {}

        public VerificationResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static VerificationResponseBuilder builder() {
            return new VerificationResponseBuilder();
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public static class VerificationResponseBuilder {
            private boolean success;
            private String message;

            public VerificationResponseBuilder success(boolean success) {
                this.success = success;
                return this;
            }

            public VerificationResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public VerificationResponse build() {
                return new VerificationResponse(success, message);
            }
        }
    }
}
