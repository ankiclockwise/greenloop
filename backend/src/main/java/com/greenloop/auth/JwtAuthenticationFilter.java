package com.greenloop.auth;

import com.greenloop.service.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract Bearer token from Authorization header
            String token = extractBearerToken(request);

            if (token != null && !token.isEmpty()) {
                try {
                    // Validate JWT and extract claims
                    if (jwtTokenProvider.validateToken(token)) {
                        String userId = jwtTokenProvider.getUserIdFromToken(token);
                        String role = jwtTokenProvider.getRoleFromToken(token);

                        // Create authorities collection with the user's role
                        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (role != null && !role.isEmpty()) {
                            // Ensure role has ROLE_ prefix for Spring Security
                            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            authorities.add(new SimpleGrantedAuthority(roleName));
                        }

                        // Create authentication token
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (ExpiredJwtException e) {
                    // Token has expired - return 401 with WWW-Authenticate header
                    handleTokenExpired(response);
                    return;
                } catch (JwtException e) {
                    // Invalid token - just continue without authentication
                    logger.debug("Invalid JWT token: " + e.getMessage());
                } catch (Exception e) {
                    logger.error("Error processing JWT token: " + e.getMessage());
                }
            }

            // Continue with the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Cannot set user authentication: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Extract Bearer token from Authorization header
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Handle expired token by returning 401 with WWW-Authenticate header
     */
    private void handleTokenExpired(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setHeader("WWW-Authenticate", "Bearer realm=\"GreenLoop\", error=\"token_expired\", error_description=\"The access token has expired\"");

        String jsonResponse = "{\"error\": \"token_expired\", \"message\": \"The access token has expired. Please refresh your token.\"}";
        response.getWriter().write(jsonResponse);
    }

    /**
     * Override shouldNotFilter to skip JWT filtering for certain URLs
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Don't filter these paths - they should be handled by Spring Security's public endpoints
        return path.startsWith("/api/auth/") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/login/") ||
               path.startsWith("/api/public/") ||
               path.equals("/health");
    }
}
