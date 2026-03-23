package com.minitalk.domain.call.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_history")
public class CallHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "caller_id", nullable = false)
    private Long callerId;

    @Column(name = "call_type", nullable = false, length = 10)
    private String callType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CallStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CallHistory() {
    }

    public static CallHistory create(Long roomId, Long callerId, String callType) {
        CallHistory call = new CallHistory();
        call.roomId = roomId;
        call.callerId = callerId;
        call.callType = callType;
        call.status = CallStatus.RINGING;
        call.createdAt = LocalDateTime.now();
        return call;
    }

    public void accept() {
        this.status = CallStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = CallStatus.REJECTED;
        this.endedAt = LocalDateTime.now();
    }

    public void end() {
        this.status = CallStatus.ENDED;
        this.endedAt = LocalDateTime.now();
        if (startedAt != null) {
            this.durationSeconds = (int) java.time.Duration.between(startedAt, endedAt).getSeconds();
        }
    }

    public void miss() {
        this.status = CallStatus.MISSED;
        this.endedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public Long getCallerId() { return callerId; }
    public String getCallType() { return callType; }
    public CallStatus getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
