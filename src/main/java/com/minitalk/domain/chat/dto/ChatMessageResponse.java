package com.minitalk.domain.chat.dto;

import com.minitalk.domain.chat.document.Message;
import java.time.Instant;
import java.util.List;

public record ChatMessageResponse(
    String messageId,
    Long roomId,
    Long senderId,
    String senderName,
    String senderAvatar,
    String content,
    String type,
    Message.Attachment attachment,
    List<Message.ReadReceipt> readBy,
    List<Message.Reaction> reactions,
    boolean edited,
    boolean deleted,
    Instant createdAt
) {

    public static ChatMessageResponse from(Message message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getRoomId(),
            message.getSenderId(),
            message.getSenderName(),
            message.getSenderAvatar(),
            message.getContent(),
            message.getType(),
            message.getAttachment(),
            message.getReadBy(),
            message.getReactions(),
            message.isEdited(),
            message.isDeleted(),
            message.getCreatedAt()
        );
    }
}
