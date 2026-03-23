package com.minitalk.domain.presence.listener;

import com.minitalk.domain.presence.service.PresenceService;
import com.minitalk.global.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(PresenceEventListener.class);

    private final PresenceService presenceService;

    public PresenceEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        Long userId = extractUserId(event.getUser());
        if (userId != null) {
            presenceService.setOnline(userId);
            log.debug("User {} connected via WebSocket", userId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Long userId = extractUserId(event.getUser());
        if (userId != null) {
            presenceService.setOffline(userId);
            log.debug("User {} disconnected from WebSocket", userId);
        }
    }

    private Long extractUserId(java.security.Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object details = auth.getPrincipal();
            if (details instanceof CustomUserDetails userDetails) {
                return userDetails.id();
            }
            try {
                return Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (principal != null) {
            try {
                return Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
