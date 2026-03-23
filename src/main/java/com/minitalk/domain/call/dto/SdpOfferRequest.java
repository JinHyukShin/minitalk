package com.minitalk.domain.call.dto;

public record SdpOfferRequest(
    Long callId,
    String sdpOffer
) {}
