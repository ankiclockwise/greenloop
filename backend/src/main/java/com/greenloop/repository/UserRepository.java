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

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByActiveTrue();

    List<User> findByUniversityVerifiedTrue();

    List<User> findByRoleAndActiveTrue(UserRole role);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email LIKE '%@%.edu'")
    List<User> findUniversityEmailUsers();

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    List<User> findByCity(String city);

    default List<User> findAllRetailers() {
        return findByRoleAndActiveTrue(UserRole.RETAILER);
    }

    default List<User> findAllDiningHalls() {
        return findByRoleAndActiveTrue(UserRole.DINING_HALL);
    }

    default List<User> findAllDonors() {
        return findByRoleAndActiveTrue(UserRole.DONOR);
    }

    default List<User> findAllConsumers() {
        return findByRoleAndActiveTrue(UserRole.CONSUMER);
    }
}
