package com.greenloop.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for email verification token operations.
 * Handles database queries and persistence for verification tokens.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    /**
     * Finds a verification token by its token string.
     *
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Counts the number of tokens created for a user after a specific time.
     * Used for rate limiting resend verification requests.
     *
     * @param userId the user ID
     * @param createdAfter the minimum creation time
     * @return count of tokens created after the specified time
     */
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.createdAt > :createdAfter")
    long countRecentTokensForUser(@Param("userId") Long userId, @Param("createdAfter") LocalDateTime createdAfter);

    /**
     * Invalidates all tokens for a specific user by marking them as used.
     * Called when a new token is generated to prevent using old tokens.
     *
     * @param userId the user ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerificationToken t SET t.isUsed = true, t.usedAt = :now WHERE t.user.id = :userId AND t.isUsed = false")
    void invalidateAllTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Invalidates all tokens for a specific user.
     * Default implementation calls the method with current time.
     *
     * @param userId the user ID
     */
    default void invalidateAllTokensForUser(Long userId) {
        invalidateAllTokensForUser(userId, LocalDateTime.now());
    }
}
