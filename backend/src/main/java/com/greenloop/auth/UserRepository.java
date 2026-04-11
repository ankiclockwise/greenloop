package com.greenloop.repository;

import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users with a specific role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all active users
     */
    List<User> findByActiveTrue();

    /**
     * Find all users with university verification
     */
    List<User> findByUniversityVerifiedTrue();

    /**
     * Find users by role and active status
     */
    List<User> findByRoleAndActiveTrue(UserRole role);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find users with .edu email addresses
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE '%@%.edu'")
    List<User> findUniversityEmailUsers();

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Find users by city (for donation/availability matching)
     */
    List<User> findByCity(String city);

    /**
     * Find all retailers
     */
    default List<User> findAllRetailers() {
        return findByRoleAndActiveTrue(UserRole.RETAILER);
    }

    /**
     * Find all dining halls
     */
    default List<User> findAllDiningHalls() {
        return findByRoleAndActiveTrue(UserRole.DINING_HALL);
    }

    /**
     * Find all donors
     */
    default List<User> findAllDonors() {
        return findByRoleAndActiveTrue(UserRole.DONOR);
    }

    /**
     * Find all consumers
     */
    default List<User> findAllConsumers() {
        return findByRoleAndActiveTrue(UserRole.CONSUMER);
    }
}
