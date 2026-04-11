package com.greenloop.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling email verification workflows including token generation,
 * validation, and email sending for the GreenLoop university system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final VerificationEmailTemplate emailTemplate;

    @Value("${greenloop.app.base-url}")
    private String baseUrl;

    @Value("${greenloop.app.from-email}")
    private String fromEmail;

    @Value("${greenloop.verification.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${greenloop.verification.rate-limit-per-hour:3}")
    private int rateLimitPerHour;

    /**
     * Generates a new verification token for the given user.
     * Creates a UUID token and stores it in the database with 24-hour expiry.
     *
     * @param userId the ID of the user
     * @return the generated verification token string
     * @throws UserNotFoundException if user does not exist
     */
    @Transactional
    public String generateVerificationToken(Long userId) {
        log.info("Generating verification token for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Invalidate any existing tokens for this user
        tokenRepository.invalidateAllTokensForUser(userId);

        String tokenString = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(tokenExpiryHours);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(tokenString)
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        tokenRepository.save(token);
        log.info("Verification token generated successfully for user ID: {}", userId);

        return tokenString;
    }

    /**
     * Sends a verification email to the user with a link to verify their email address.
     * Uses HTML email template with GreenLoop branding.
     *
     * @param user the user to send the verification email to
     * @param token the verification token
     * @throws MessagingException if email sending fails
     */
    @Transactional
    public void sendVerificationEmail(User user, String token) {
        log.info("Sending verification email to user: {}", user.getEmail());

        if (!isUniversityEmail(user.getEmail())) {
            log.warn("Attempted to send verification email to non-university email: {}", user.getEmail());
            throw new InvalidEmailException("Email must be a valid university (.edu) email address");
        }

        String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your GreenLoop Account Email");

            String htmlContent = emailTemplate.buildVerificationEmailHtml(
                    user.getFirstName(),
                    verificationUrl,
                    tokenExpiryHours
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent successfully to user: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send verification email to user: {}", user.getEmail(), e);
            throw new EmailSendingException("Failed to send verification email", e);
        }
    }

    /**
     * Verifies an email verification token and marks the user as verified.
     * Invalidates the token after successful verification.
     *
     * @param token the verification token string
     * @throws TokenExpiredException if token has expired
     * @throws TokenAlreadyUsedException if token has already been used
     * @throws TokenNotFoundException if token does not exist
     */
    @Transactional
    public void verifyToken(String token) {
        log.info("Attempting to verify token");

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Verification token not found"));

        if (verificationToken.isExpired()) {
            log.warn("Verification token has expired for user: {}", verificationToken.getUser().getId());
            throw new TokenExpiredException("Verification token has expired");
        }

        if (verificationToken.isUsed()) {
            log.warn("Verification token has already been used for user: {}", verificationToken.getUser().getId());
            throw new TokenAlreadyUsedException("Verification token has already been used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        verificationToken.setIsUsed(true);
        tokenRepository.save(verificationToken);

        log.info("User email verified successfully for user ID: {}", user.getId());
    }

    /**
     * Validates whether an email address is a valid university email (.edu domain).
     *
     * @param email the email address to validate
     * @return true if email ends in .edu, false otherwise
     */
    public boolean isUniversityEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        return email.toLowerCase().endsWith(".edu");
    }

    /**
     * Resends a verification email to the given email address.
     * Rate-limited to a maximum of 3 resends per hour per email address.
     *
     * @param email the email address to resend verification to
     * @throws RateLimitExceededException if maximum resend limit exceeded
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void resendVerification(String email) {
        log.info("Resend verification requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            log.warn("Attempted to resend verification for already verified user: {}", email);
            throw new EmailAlreadyVerifiedException("This email address is already verified");
        }

        // Check rate limit
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTokenCount = tokenRepository.countRecentTokensForUser(user.getId(), oneHourAgo);

        if (recentTokenCount >= rateLimitPerHour) {
            log.warn("Rate limit exceeded for email verification resend: {}", email);
            throw new RateLimitExceededException(
                    "Maximum email resend limit reached. Please try again in an hour."
            );
        }

        String newToken = generateVerificationToken(user.getId());
        sendVerificationEmail(user, newToken);

        log.info("Verification email resent successfully to: {}", email);
    }
}
