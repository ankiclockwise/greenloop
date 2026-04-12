package com.greenloop.auth.security;

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
            String token = extractBearerToken(request);

            if (token != null && !token.isEmpty()) {
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        String userId = jwtTokenProvider.getUserIdFromToken(token);
                        String role = jwtTokenProvider.getRoleFromToken(token);

                        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (role != null && !role.isEmpty()) {
                            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            authorities.add(new SimpleGrantedAuthority(roleName));
                        }

                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userId, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (ExpiredJwtException e) {
                    handleTokenExpired(response);
                    return;
                } catch (JwtException e) {
                    logger.debug("Invalid JWT token: " + e.getMessage());
                } catch (Exception e) {
                    logger.error("Error processing JWT token: " + e.getMessage());
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Cannot set user authentication: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void handleTokenExpired(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setHeader("WWW-Authenticate",
                "Bearer realm=\"GreenLoop\", error=\"token_expired\", error_description=\"The access token has expired\"");
        response.getWriter().write(
                "{\"error\": \"token_expired\", \"message\": \"The access token has expired. Please refresh your token.\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/login/") ||
               path.startsWith("/api/public/") ||
               path.equals("/health");
    }
}
