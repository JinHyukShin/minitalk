package com.minitalk.domain.chat.service;

import com.minitalk.domain.chat.repository.MessageRepository;
import com.minitalk.domain.chat.repository.MessageRepositoryCustom;
import com.minitalk.domain.room.repository.ChatRoomMemberRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadReceiptService {

    private final MessageRepository messageRepository;
    private final MessageRepositoryCustom messageRepositoryCustom;
    private final ChatRoomMemberRepository memberRepository;

    public ReadReceiptService(MessageRepository messageRepository,
                              MessageRepositoryCustom messageRepositoryCustom,
                              ChatRoomMemberRepository memberRepository) {
        this.messageRepository = messageRepository;
        this.messageRepositoryCustom = messageRepositoryCustom;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void markAsRead(Long roomId, Long userId, String lastMessageId) {
        messageRepositoryCustom.markAsReadBulk(roomId, userId, lastMessageId);
        memberRepository.updateLastReadAt(roomId, userId, LocalDateTime.now());
    }

    public int getUnreadCount(Long roomId, Long userId) {
        Optional<LocalDateTime> lastReadAt = memberRepository.getLastReadAt(roomId, userId);
        if (lastReadAt.isEmpty()) {
            return (int) messageRepository.countByRoomId(roomId);
        }
        return (int) messageRepository.countByRoomIdAndCreatedAtAfter(
            roomId, lastReadAt.get().atZone(ZoneId.systemDefault()).toInstant());
    }
}
