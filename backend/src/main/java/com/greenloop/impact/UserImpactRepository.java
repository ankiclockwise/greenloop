package com.greenloop.impact;

import com.greenloop.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserImpactRepository extends JpaRepository<UserImpact, Long> {

    @Query("SELECT ui FROM UserImpact ui WHERE ui.user.id = :userId")
    Optional<UserImpact> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ui FROM UserImpact ui WHERE ui.user.email = :email")
    Optional<UserImpact> findByUserEmail(@Param("email") String email);

    @Query("SELECT ui FROM UserImpact ui WHERE ui.user.role IN :roles ORDER BY ui.co2SavedKg DESC")
    List<UserImpact> findByUserRolesOrderByCo2(@Param("roles") List<UserRole> roles);
}
