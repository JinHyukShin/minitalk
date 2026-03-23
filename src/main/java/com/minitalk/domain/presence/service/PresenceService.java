package com.minitalk.domain.presence.service;

import com.minitalk.domain.presence.dto.PresenceEvent;
import com.minitalk.domain.presence.dto.PresenceResponse;
import com.minitalk.infra.redis.RedisPublisher;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

    private static final Duration PRESENCE_TTL = Duration.ofSeconds(300);

    private final StringRedisTemplate redisTemplate;
    private final RedisPublisher redisPublisher;

    public PresenceService(StringRedisTemplate redisTemplate, RedisPublisher redisPublisher) {
        this.redisTemplate = redisTemplate;
        this.redisPublisher = redisPublisher;
    }

    public void setOnline(Long userId) {
        String key = "presence:user:" + userId;
        redisTemplate.opsForHash().put(key, "status", "ONLINE");
        redisTemplate.opsForHash().put(key, "lastSeen", Instant.now().toString());
        redisTemplate.expire(key, PRESENCE_TTL);

        redisPublisher.publish("channel:presence", new PresenceEvent(userId, "ONLINE"));
    }

    public void setOffline(Long userId) {
        String key = "presence:user:" + userId;
        redisTemplate.opsForHash().put(key, "status", "OFFLINE");
        redisTemplate.opsForHash().put(key, "lastSeen", Instant.now().toString());
        redisTemplate.expire(key, PRESENCE_TTL);

        redisPublisher.publish("channel:presence", new PresenceEvent(userId, "OFFLINE"));
    }

    public void heartbeat(Long userId) {
        String key = "presence:user:" + userId;
        redisTemplate.opsForHash().put(key, "lastSeen", Instant.now().toString());
        redisTemplate.expire(key, PRESENCE_TTL);
    }

    public boolean isOnline(Long userId) {
        Object status = redisTemplate.opsForHash().get("presence:user:" + userId, "status");
        return "ONLINE".equals(status);
    }

    public PresenceResponse getPresence(Long userId) {
        String key = "presence:user:" + userId;
        Object status = redisTemplate.opsForHash().get(key, "status");
        Object lastSeen = redisTemplate.opsForHash().get(key, "lastSeen");

        return new PresenceResponse(
            userId,
            status != null ? status.toString() : "OFFLINE",
            lastSeen != null ? lastSeen.toString() : null
        );
    }

    public Map<Long, Boolean> getOnlineStatuses(List<Long> userIds) {
        return userIds.stream()
            .collect(Collectors.toMap(id -> id, this::isOnline));
    }

    public List<PresenceResponse> getOnlineUsers(List<Long> userIds) {
        List<PresenceResponse> onlineUsers = new ArrayList<>();
        for (Long userId : userIds) {
            if (isOnline(userId)) {
                onlineUsers.add(getPresence(userId));
            }
        }
        return onlineUsers;
    }
}
