package com.minitalk.domain.presence.controller;

import com.minitalk.domain.presence.service.PresenceService;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PresenceStompController {

    private final PresenceService presenceService;

    public PresenceStompController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @MessageMapping("/presence/heartbeat")
    public void heartbeat(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        presenceService.heartbeat(userId);
    }
}
