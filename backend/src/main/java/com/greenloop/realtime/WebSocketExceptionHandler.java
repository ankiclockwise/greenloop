package com.greenloop.realtime;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for WebSocket STOMP messaging.
 *
 * Handles exceptions thrown in message-mapped methods and sends error responses
 * back to clients through the /queue/errors/{user} endpoint.
 *
 * @author GreenLoop Team
 * @since 1.0.0
 */
@Controller
public class WebSocketExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketExceptionHandler.class);

    /**
     * Handles exceptions in message mapping methods.
     *
     * Sends error details back to the client for display/logging.
     *
     * @param exception the exception that occurred
     * @return error DTO to send to client
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public WebSocketErrorDto handleException(Exception exception) {
        log.error("WebSocket message handling error: {}", exception.getMessage(), exception);

        WebSocketErrorDto error = new WebSocketErrorDto();
        error.setError(exception.getClass().getSimpleName());
        error.setMessage(exception.getMessage());
        error.setTimestamp(System.currentTimeMillis());

        return error;
    }

    /**
     * DTO for WebSocket error responses.
     */
    public static class WebSocketErrorDto {
        private String error;
        private String message;
        private long timestamp;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
