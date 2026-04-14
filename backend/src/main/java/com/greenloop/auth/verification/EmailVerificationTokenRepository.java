package com.greenloop.auth.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.createdAt > :createdAfter")
    long countRecentTokensForUser(@Param("userId") Long userId, @Param("createdAfter") LocalDateTime createdAfter);

    @Modifying
    @Transactional
    @Query("UPDATE EmailVerificationToken t SET t.isUsed = true, t.usedAt = :now WHERE t.user.id = :userId AND t.isUsed = false")
    void invalidateAllTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    default void invalidateAllTokensForUser(Long userId) {
        invalidateAllTokensForUser(userId, LocalDateTime.now());
    }
}
