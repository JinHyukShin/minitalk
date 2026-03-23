package com.minitalk.domain.chat.controller;

import com.minitalk.domain.chat.document.Message;
import com.minitalk.domain.chat.dto.ChatMessageRequest;
import com.minitalk.domain.chat.dto.ChatMessageResponse;
import com.minitalk.domain.chat.dto.ReadReceiptEvent;
import com.minitalk.domain.chat.dto.ReadReceiptRequest;
import com.minitalk.domain.chat.dto.TypingEvent;
import com.minitalk.domain.chat.dto.TypingRequest;
import com.minitalk.domain.chat.service.ChatService;
import com.minitalk.domain.chat.service.ReadReceiptService;
import com.minitalk.domain.auth.entity.User;
import com.minitalk.domain.auth.repository.UserRepository;
import com.minitalk.infra.redis.RedisPublisher;
import java.security.Principal;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatStompController {

    private final ChatService chatService;
    private final ReadReceiptService readReceiptService;
    private final RedisPublisher redisPublisher;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    public ChatStompController(ChatService chatService,
                               ReadReceiptService readReceiptService,
                               RedisPublisher redisPublisher,
                               StringRedisTemplate redisTemplate,
                               UserRepository userRepository) {
        this.chatService = chatService;
        this.readReceiptService = readReceiptService;
        this.redisPublisher = redisPublisher;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload ChatMessageRequest request,
                            Principal principal) {
        Long senderId = Long.parseLong(principal.getName());

        chatService.validateMembership(roomId, senderId);

        Message message = chatService.saveMessage(roomId, senderId, request);
        ChatMessageResponse response = ChatMessageResponse.from(message);

        redisPublisher.publish("channel:room:" + roomId, response);
    }

    @MessageMapping("/chat/{roomId}/typing")
    public void typing(@DestinationVariable Long roomId,
                       @Payload TypingRequest request,
                       Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        String userName = userRepository.findById(userId)
            .map(User::getName).orElse("Unknown");

        if (request.typing()) {
            redisTemplate.opsForValue().set(
                "typing:room:" + roomId + ":" + userId, "1",
                Duration.ofSeconds(3));
        } else {
            redisTemplate.delete("typing:room:" + roomId + ":" + userId);
        }

        redisPublisher.publish("channel:room:" + roomId + ":typing",
            new TypingEvent(roomId, userId, userName, request.typing()));
    }

    @MessageMapping("/chat/{roomId}/read")
    public void readReceipt(@DestinationVariable Long roomId,
                            @Payload ReadReceiptRequest request,
                            Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        readReceiptService.markAsRead(roomId, userId, request.lastMessageId());

        redisPublisher.publish("channel:room:" + roomId + ":read",
            new ReadReceiptEvent(roomId, userId, request.lastMessageId()));
    }
}
