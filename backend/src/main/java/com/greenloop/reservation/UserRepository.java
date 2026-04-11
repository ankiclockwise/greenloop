package com.greenloop.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 *
 * Provides database access for user-related queries, including account status and no-show tracking.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<Object, Long> {

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return optional containing the user, or empty if not found
     */
    Optional<?> findById(Long id);

    /**
     * Finds all users flagged for account review.
     *
     * Users are flagged when they reach or exceed the no-show threshold.
     *
     * @return list of users under review
     */
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'UNDER_REVIEW'")
    List<?> findUnderReview();

    /**
     * Finds all users with a specific account status.
     *
     * @param status the account status to filter by
     * @return list of matching users
     */
    @Query("SELECT u FROM User u WHERE u.accountStatus = :status")
    List<?> findByAccountStatus(@Param("status") String status);

    /**
     * Counts users with a specific account status.
     *
     * @param status the account status
     * @return the count of users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountStatus = :status")
    long countByAccountStatus(@Param("status") String status);

    /**
     * Finds users with a no-show count at or above a threshold.
     *
     * @param threshold the minimum no-show count
     * @return list of users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE u.noShowCount >= :threshold")
    List<?> findByNoShowCountGreaterThanOrEqual(@Param("threshold") int threshold);
}
