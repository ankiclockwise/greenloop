package com.greenloop.auth.oauth;

import com.greenloop.auth.security.JwtTokenProvider;
import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
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
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) return;

        OAuth2User oauth2User = oauth2Token.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
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
            user.setName(name);
            user.setProfilePictureUrl(picture);
            user.setUpdatedAt(LocalDateTime.now());
            if (!user.isUniversityVerified() && isUniversityEmail(email)) {
                user.setUniversityVerified(true);
            }
            user = userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        String dashboardUrl = roleDashboardRedirectService.getDashboardUrl(user.getRole());

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

    private boolean isUniversityEmail(String email) {
        return email != null && email.toLowerCase().endsWith(".edu");
    }
}
