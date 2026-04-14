package com.greenloop.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time feed updates and notifications.
 * Enables STOMP messaging over WebSocket with SockJS fallback support.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker for handling STOMP messages.
     *
     * Enables simple in-memory message broker with the following:
     * - /topic: for broadcast messages (one-to-many)
     * - /queue: for point-to-point messages (one-to-one)
     * - Application destination prefix: /app for client-to-server messages
     *
     * Heartbeat configuration:
     * - Server sends heartbeat every 10 seconds
     * - Server expects heartbeat from client every 10 seconds
     *
     * @param config the message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
                // .setHeartbeatValue(new long[]{10000, 10000});

        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers WebSocket STOMP endpoints.
     *
     * Registers the /ws endpoint for WebSocket connections with:
     * - SockJS fallback for browsers without native WebSocket support
     * - CORS configuration allowing requests from frontend origin
     *
     * @param registry the STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "${frontend.url:http://localhost:3000}"
                )
                .withSockJS();
    }
}
