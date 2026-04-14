package com.greenloop.auth.oauth;

import com.greenloop.model.UserRole;
import org.springframework.stereotype.Service;

@Service
public class RoleDashboardRedirectService {

    public String getDashboardUrl(UserRole role) {
        if (role == null) return "/feed";
        return switch (role) {
            case CONSUMER -> "/feed";
            case RETAILER -> "/retailer/dashboard";
            case DINING_HALL -> "/dining/dashboard";
            case DONOR -> "/donor/listings";
            case ADMIN -> "/admin/dashboard";
            default -> "/feed";
        };
    }

    public String getRoleDisplayName(UserRole role) {
        if (role == null) return "Consumer";
        return switch (role) {
            case CONSUMER -> "Consumer";
            case RETAILER -> "Retailer";
            case DINING_HALL -> "Dining Hall";
            case DONOR -> "Donor";
            case ADMIN -> "Administrator";
            default -> "User";
        };
    }

    public boolean isAdmin(UserRole role) {
        return role == UserRole.ADMIN;
    }

    public boolean isBusinessUser(UserRole role) {
        return role == UserRole.RETAILER || role == UserRole.DINING_HALL || role == UserRole.DONOR;
    }
}
