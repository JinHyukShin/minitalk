package com.minitalk.domain.call.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_participant")
public class CallParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", nullable = false)
    private Long callId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    protected CallParticipant() {
    }

    public static CallParticipant create(Long callId, Long userId) {
        CallParticipant participant = new CallParticipant();
        participant.callId = callId;
        participant.userId = userId;
        participant.joinedAt = LocalDateTime.now();
        return participant;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getCallId() { return callId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getLeftAt() { return leftAt; }
}
