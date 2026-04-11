package com.greenloop.auth;

import com.greenloop.model.UserRole;
import org.springframework.stereotype.Service;

@Service
public class RoleDashboardRedirectService {

    /**
     * Returns the appropriate dashboard URL based on the user's role
     *
     * @param role The user's role
     * @return The dashboard path for the given role
     */
    public String getDashboardUrl(UserRole role) {
        if (role == null) {
            return "/feed"; // Default to consumer feed
        }

        return switch (role) {
            case CONSUMER -> "/feed";
            case RETAILER -> "/retailer/dashboard";
            case DINING_HALL -> "/dining/dashboard";
            case DONOR -> "/donor/listings";
            case ADMIN -> "/admin/dashboard";
            default -> "/feed";
        };
    }

    /**
     * Get display name for role
     */
    public String getRoleDisplayName(UserRole role) {
        if (role == null) {
            return "Consumer";
        }

        return switch (role) {
            case CONSUMER -> "Consumer";
            case RETAILER -> "Retailer";
            case DINING_HALL -> "Dining Hall";
            case DONOR -> "Donor";
            case ADMIN -> "Administrator";
            default -> "User";
        };
    }

    /**
     * Check if user can access admin features
     */
    public boolean isAdmin(UserRole role) {
        return role == UserRole.ADMIN;
    }

    /**
     * Check if user is a business user (Retailer, Dining Hall, or Donor)
     */
    public boolean isBusinessUser(UserRole role) {
        return role == UserRole.RETAILER || role == UserRole.DINING_HALL || role == UserRole.DONOR;
    }
}
