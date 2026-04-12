package com.greenloop.auth.verification;

import com.greenloop.auth.exception.EmailAlreadyVerifiedException;
import com.greenloop.auth.exception.RateLimitExceededException;
import com.greenloop.auth.exception.TokenAlreadyUsedException;
import com.greenloop.auth.exception.TokenExpiredException;
import com.greenloop.auth.exception.TokenNotFoundException;
import com.greenloop.auth.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/auth")
public class EmailVerificationController {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationController.class);

    private final EmailVerificationService verificationService;

    public EmailVerificationController(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerificationResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            verificationService.verifyToken(request.getToken());
            return ResponseEntity.ok(VerificationResponse.of(true, "Email verified successfully"));
        } catch (TokenExpiredException | TokenAlreadyUsedException | TokenNotFoundException e) {
            log.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(VerificationResponse.of(false, e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<VerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            verificationService.resendVerification(request.getEmail());
            return ResponseEntity.ok(VerificationResponse.of(true, "Verification email sent successfully"));
        } catch (EmailAlreadyVerifiedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(VerificationResponse.of(false, e.getMessage()));
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(VerificationResponse.of(false, e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(VerificationResponse.of(false, "No account found with this email address"));
        }
    }

    public static class VerifyEmailRequest {
        @NotBlank(message = "Verification token is required")
        private String token;
        public VerifyEmailRequest() {}
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class ResendVerificationRequest {
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        private String email;
        public ResendVerificationRequest() {}
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class VerificationResponse {
        private boolean success;
        private String message;
        public VerificationResponse() {}
        public static VerificationResponse of(boolean success, String message) {
            VerificationResponse r = new VerificationResponse();
            r.success = success;
            r.message = message;
            return r;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
