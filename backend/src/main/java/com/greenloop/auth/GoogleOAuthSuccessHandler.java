package com.greenloop.auth;

import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
import com.greenloop.service.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class GoogleOAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RoleDashboardRedirectService roleDashboardRedirectService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();

            // Extract user information from OAuth2User
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");

            // Check if user exists in database
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Create new user with Consumer as default role
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setProfilePictureUrl(picture);
                user.setRole(UserRole.CONSUMER);
                user.setUniversityVerified(isUniversityEmail(email));
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setActive(true);

                user = userRepository.save(user);
            } else {
                // Update existing user with latest info from Google
                user.setName(name);
                user.setProfilePictureUrl(picture);
                user.setUpdatedAt(LocalDateTime.now());

                // If not yet university verified but email is .edu domain, mark as verified
                if (!user.isUniversityVerified() && isUniversityEmail(email)) {
                    user.setUniversityVerified(true);
                }

                user = userRepository.save(user);
            }

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // Get dashboard URL based on user role
            String dashboardUrl = roleDashboardRedirectService.getDashboardUrl(user.getRole());

            // Redirect to frontend with tokens and role as query parameters
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path(dashboardUrl)
                    .queryParam("token", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("role", user.getRole().name())
                    .queryParam("userId", user.getId())
                    .queryParam("name", user.getName())
                    .queryParam("email", user.getEmail())
                    .queryParam("picture", user.getProfilePictureUrl())
                    .queryParam("universityVerified", user.isUniversityVerified())
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Check if the email is a university email (ends with .edu)
     */
    private boolean isUniversityEmail(String email) {
        return email != null && email.toLowerCase().endsWith(".edu");
    }
}
