package com.minitalk.domain.chat.service;

import com.minitalk.domain.auth.entity.User;
import com.minitalk.domain.auth.repository.UserRepository;
import com.minitalk.domain.chat.document.Message;
import com.minitalk.domain.chat.dto.ChatMessageRequest;
import com.minitalk.domain.chat.dto.ChatMessageResponse;
import com.minitalk.domain.chat.repository.MessageRepository;
import com.minitalk.domain.chat.repository.MessageRepositoryCustom;
import com.minitalk.domain.room.repository.ChatRoomMemberRepository;
import com.minitalk.global.exception.BusinessException;
import com.minitalk.global.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final MessageRepositoryCustom messageRepositoryCustom;
    private final UserRepository userRepository;
    private final ChatRoomMemberRepository memberRepository;

    public ChatService(MessageRepository messageRepository,
                       MessageRepositoryCustom messageRepositoryCustom,
                       UserRepository userRepository,
                       ChatRoomMemberRepository memberRepository) {
        this.messageRepository = messageRepository;
        this.messageRepositoryCustom = messageRepositoryCustom;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    public void validateMembership(Long roomId, Long userId) {
        if (!memberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(ErrorCode.ROOM_NOT_MEMBER);
        }
    }

    public Message saveMessage(Long roomId, Long senderId, ChatMessageRequest request) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message message = Message.create(
            roomId, senderId, sender.getName(), sender.getAvatarUrl(),
            request.type() != null ? request.type() : "TEXT",
            request.content()
        );

        return messageRepository.save(message);
    }

    public Message saveAndBroadcastFileMessage(Long roomId, Long senderId,
                                                Long fileId, String fileName,
                                                long fileSize, String mimeType,
                                                String url, String thumbnailUrl,
                                                String messageType) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Message message = Message.create(
            roomId, senderId, sender.getName(), sender.getAvatarUrl(),
            messageType, fileName
        );
        message.setAttachment(new Message.Attachment(
            fileId, fileName, fileSize, mimeType, url, thumbnailUrl));

        return messageRepository.save(message);
    }

    public List<ChatMessageResponse> getMessages(Long roomId, String cursor, int size) {
        List<Message> messages;
        PageRequest pageRequest = PageRequest.of(0, size);

        if (cursor != null) {
            Instant cursorInstant = Instant.parse(cursor);
            messages = messageRepository.findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                roomId, cursorInstant, pageRequest);
        } else {
            messages = messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageRequest);
        }

        return messages.stream().map(ChatMessageResponse::from).toList();
    }

    public List<ChatMessageResponse> searchMessages(Long roomId, String query, int page, int size) {
        return messageRepositoryCustom.searchMessages(roomId, query, page, size)
            .stream().map(ChatMessageResponse::from).toList();
    }

    public void markAsRead(Long roomId, Long userId, String lastMessageId) {
        messageRepositoryCustom.markAsReadBulk(roomId, userId, lastMessageId);
    }

    public ChatMessageResponse editMessage(String messageId, Long userId, String content) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHAT_NOT_MESSAGE_OWNER);
        }

        message.edit(content);
        messageRepository.save(message);
        return ChatMessageResponse.from(message);
    }

    public void deleteMessage(String messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHAT_NOT_MESSAGE_OWNER);
        }

        message.markDeleted();
        messageRepository.save(message);
    }
}
