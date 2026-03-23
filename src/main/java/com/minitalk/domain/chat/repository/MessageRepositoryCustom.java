package com.minitalk.domain.chat.repository;

import com.minitalk.domain.chat.document.Message;
import java.util.List;

public interface MessageRepositoryCustom {

    void markAsReadBulk(Long roomId, Long userId, String lastMessageId);

    List<Message> searchMessages(Long roomId, String query, int page, int size);
}
