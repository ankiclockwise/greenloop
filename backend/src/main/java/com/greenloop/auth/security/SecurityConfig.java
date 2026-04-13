package com.greenloop.auth.security;

import com.greenloop.auth.oauth.GoogleOAuthFailureHandler;
import com.greenloop.auth.oauth.GoogleOAuthSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private GoogleOAuthSuccessHandler googleOAuthSuccessHandler;

    @Autowired
    private GoogleOAuthFailureHandler googleOAuthFailureHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.frontend.production.url:https://greenloop.com}")
    private String productionUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/listings/**").permitAll()
                .requestMatchers("/api/reservations/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/feed/**").hasAnyRole("CONSUMER", "RETAILER", "DINING_HALL", "DONOR")
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("CONSUMER", "RETAILER", "DINING_HALL", "DONOR")
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("CONSUMER")
                .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("CONSUMER")
                .requestMatchers("/api/retailer/**").hasRole("RETAILER")
                .requestMatchers("/api/dining/**").hasRole("DINING_HALL")
                .requestMatchers("/api/donor/**").hasRole("DONOR")
                .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/profile").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(googleOAuthSuccessHandler)
                .failureHandler(googleOAuthFailureHandler)
                .authorizationEndpoint(auth -> auth.baseUri("/oauth2/authorize"))
                .redirectionEndpoint(redirect -> redirect.baseUri("/login/oauth2/code/*")))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            frontendUrl, productionUrl,
            "http://localhost:3000", "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type", "Authorization", "X-Requested-With",
            "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "WWW-Authenticate"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
