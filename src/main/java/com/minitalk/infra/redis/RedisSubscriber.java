package com.minitalk.infra.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minitalk.domain.chat.dto.ChatMessageResponse;
import com.minitalk.domain.chat.dto.ReadReceiptEvent;
import com.minitalk.domain.chat.dto.TypingEvent;
import com.minitalk.domain.presence.dto.PresenceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisSubscriber(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            log.debug("Received message on channel {}: {}", channel, body);

            if (channel.equals(RedisKeyPrefix.CHANNEL_PRESENCE)) {
                handlePresenceEvent(body);
            } else if (channel.contains(":typing")) {
                handleTypingEvent(body);
            } else if (channel.contains(":read")) {
                handleReadReceiptEvent(body);
            } else if (channel.startsWith(RedisKeyPrefix.CHANNEL_ROOM)) {
                handleChatMessage(channel, body);
            }

        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }

    private void handleChatMessage(String channel, String body) throws Exception {
        ChatMessageResponse msg = objectMapper.readValue(body, ChatMessageResponse.class);
        Long roomId = extractRoomId(channel);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, msg);
    }

    private void handleTypingEvent(String body) throws Exception {
        TypingEvent event = objectMapper.readValue(body, TypingEvent.class);
        messagingTemplate.convertAndSend("/topic/room/" + event.roomId() + "/typing", event);
    }

    private void handleReadReceiptEvent(String body) throws Exception {
        ReadReceiptEvent event = objectMapper.readValue(body, ReadReceiptEvent.class);
        messagingTemplate.convertAndSend("/topic/room/" + event.roomId() + "/read", event);
    }

    private void handlePresenceEvent(String body) throws Exception {
        PresenceEvent event = objectMapper.readValue(body, PresenceEvent.class);
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    private Long extractRoomId(String channel) {
        String prefix = RedisKeyPrefix.CHANNEL_ROOM;
        String remainder = channel.substring(prefix.length());
        int colonIndex = remainder.indexOf(':');
        if (colonIndex > 0) {
            return Long.parseLong(remainder.substring(0, colonIndex));
        }
        return Long.parseLong(remainder);
    }
}
