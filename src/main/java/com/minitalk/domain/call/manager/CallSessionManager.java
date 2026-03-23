package com.minitalk.domain.call.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CallSessionManager {

    private final Map<Long, CallSession> activeSessions = new ConcurrentHashMap<>();

    public void createSession(Long callId, Long callerId) {
        CallSession session = new CallSession(callId, callerId);
        activeSessions.put(callId, session);
    }

    public CallSession getSession(Long callId) {
        return activeSessions.get(callId);
    }

    public void removeSession(Long callId) {
        activeSessions.remove(callId);
    }

    public boolean hasSession(Long callId) {
        return activeSessions.containsKey(callId);
    }

    public static class CallSession {
        private final Long callId;
        private final Long callerId;
        private final Map<Long, String> participantEndpoints = new ConcurrentHashMap<>();

        public CallSession(Long callId, Long callerId) {
            this.callId = callId;
            this.callerId = callerId;
        }

        public void addParticipant(Long userId) {
            participantEndpoints.put(userId, "endpoint-placeholder-" + userId);
        }

        public void removeParticipant(Long userId) {
            participantEndpoints.remove(userId);
        }

        public int getParticipantCount() {
            return participantEndpoints.size();
        }

        public Long getCallId() { return callId; }
        public Long getCallerId() { return callerId; }
        public Map<Long, String> getParticipantEndpoints() { return participantEndpoints; }
    }
}
