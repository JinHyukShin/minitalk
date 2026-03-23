package com.minitalk.domain.call.controller;

import com.minitalk.domain.call.dto.IceCandidateMessage;
import com.minitalk.domain.call.dto.SdpAnswerResponse;
import com.minitalk.domain.call.dto.SdpOfferRequest;
import com.minitalk.infra.kurento.KurentoClientWrapper;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingStompController {

    private final KurentoClientWrapper kurentoClientWrapper;
    private final SimpMessagingTemplate messagingTemplate;

    public SignalingStompController(KurentoClientWrapper kurentoClientWrapper,
                                   SimpMessagingTemplate messagingTemplate) {
        this.kurentoClientWrapper = kurentoClientWrapper;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/signal/sdp-offer")
    public void handleSdpOffer(@Payload SdpOfferRequest request, Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        String sdpAnswer = kurentoClientWrapper.processOffer(
            request.callId(), userId, request.sdpOffer());

        messagingTemplate.convertAndSendToUser(
            principal.getName(), "/queue/signal",
            new SdpAnswerResponse(request.callId(), sdpAnswer));
    }

    @MessageMapping("/signal/ice-candidate")
    public void handleIceCandidate(@Payload IceCandidateMessage request, Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        kurentoClientWrapper.addIceCandidate(
            request.callId(), userId, request.candidate());
    }
}
