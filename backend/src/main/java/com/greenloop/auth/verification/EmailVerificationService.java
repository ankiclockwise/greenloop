package com.greenloop.auth.verification;

import com.greenloop.auth.exception.EmailAlreadyVerifiedException;
import com.greenloop.auth.exception.EmailSendingException;
import com.greenloop.auth.exception.InvalidEmailException;
import com.greenloop.auth.exception.RateLimitExceededException;
import com.greenloop.auth.exception.TokenAlreadyUsedException;
import com.greenloop.auth.exception.TokenExpiredException;
import com.greenloop.auth.exception.TokenNotFoundException;
import com.greenloop.auth.exception.UserNotFoundException;
import com.greenloop.model.User;
import com.greenloop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final VerificationEmailTemplate emailTemplate;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository, JavaMailSender mailSender, VerificationEmailTemplate emailTemplate) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.emailTemplate = emailTemplate;
    }

    @Value("${greenloop.app.base-url}")
    private String baseUrl;

    @Value("${greenloop.app.from-email}")
    private String fromEmail;

    @Value("${greenloop.verification.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${greenloop.verification.rate-limit-per-hour:3}")
    private int rateLimitPerHour;

    @Transactional
    public String generateVerificationToken(Long userId) {
        log.info("Generating verification token for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        tokenRepository.invalidateAllTokensForUser(userId);

        String tokenString = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationToken token = new EmailVerificationToken(
                null,
                tokenString,
                user,
                now,
                now.plusHours(tokenExpiryHours),
                null,
                false
        );

        tokenRepository.save(token);
        log.info("Verification token generated for user ID: {}", userId);
        return tokenString;
    }

    @Transactional
    public void sendVerificationEmail(User user, String token) {
        log.info("Sending verification email to: {}", user.getEmail());

        if (!isUniversityEmail(user.getEmail())) {
            throw new InvalidEmailException("Email must be a valid university (.edu) email address");
        }

        String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your GreenLoop Account Email");
            helper.setText(emailTemplate.buildVerificationEmailHtml(
                    user.getName(), verificationUrl, tokenExpiryHours), true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new EmailSendingException("Failed to send verification email", e);
        }
    }

    @Transactional
    public void verifyToken(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Verification token not found"));

        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("Verification token has expired");
        }
        if (verificationToken.getIsUsed()) {
            throw new TokenAlreadyUsedException("Verification token has already been used");
        }

        User user = verificationToken.getUser();
        user.setUniversityVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        verificationToken.setIsUsed(true);
        tokenRepository.save(verificationToken);

        log.info("Email verified for user ID: {}", user.getId());
    }

    public boolean isUniversityEmail(String email) {
        return email != null && email.contains("@") && email.toLowerCase().endsWith(".edu");
    }

    @Transactional
    public void resendVerification(String email) {
        log.info("Resend verification requested for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (user.isUniversityVerified()) {
            throw new EmailAlreadyVerifiedException("This email address is already verified");
        }

        long recentTokenCount = tokenRepository.countRecentTokensForUser(
                user.getId(), LocalDateTime.now().minusHours(1));

        if (recentTokenCount >= rateLimitPerHour) {
            throw new RateLimitExceededException("Maximum email resend limit reached. Please try again in an hour.");
        }

        String newToken = generateVerificationToken(user.getId());
        sendVerificationEmail(user, newToken);
        log.info("Verification email resent to: {}", email);
    }
}
