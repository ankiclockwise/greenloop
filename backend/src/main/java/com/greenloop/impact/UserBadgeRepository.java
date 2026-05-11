package com.greenloop.impact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId")
    List<UserBadge> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId AND ub.badgeId = :badgeId")
    Optional<UserBadge> findByUserIdAndBadgeId(@Param("userId") Long userId, @Param("badgeId") String badgeId);
}
