package com.minitalk.domain.chat.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(name = "idx_room_created", def = "{'roomId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "idx_room_readby", def = "{'roomId': 1, 'readBy.userId': 1}"),
    @CompoundIndex(name = "idx_sender_created", def = "{'senderId': 1, 'createdAt': -1}")
})
public class Message {

    @Id
    private String id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String type;
    private String content;
    private Attachment attachment;
    private SystemEvent systemEvent;
    private List<ReadReceipt> readBy = new ArrayList<>();
    private List<Reaction> reactions = new ArrayList<>();
    private boolean edited;
    private Instant editedAt;
    private boolean deleted;
    private Instant deletedAt;
    private Instant createdAt;

    public static Message create(Long roomId, Long senderId, String senderName,
                                  String senderAvatar, String type, String content) {
        Message message = new Message();
        message.roomId = roomId;
        message.senderId = senderId;
        message.senderName = senderName;
        message.senderAvatar = senderAvatar;
        message.type = type;
        message.content = content;
        message.createdAt = Instant.now();
        message.readBy.add(new ReadReceipt(senderId, Instant.now()));
        return message;
    }

    public void edit(String content) {
        this.content = content;
        this.edited = true;
        this.editedAt = Instant.now();
    }

    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.content = null;
    }

    public void addReadReceipt(Long userId) {
        if (readBy.stream().noneMatch(r -> r.userId().equals(userId))) {
            readBy.add(new ReadReceipt(userId, Instant.now()));
        }
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public void setSystemEvent(SystemEvent systemEvent) {
        this.systemEvent = systemEvent;
    }

    public String getId() { return id; }
    public Long getRoomId() { return roomId; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public Attachment getAttachment() { return attachment; }
    public SystemEvent getSystemEvent() { return systemEvent; }
    public List<ReadReceipt> getReadBy() { return readBy; }
    public List<Reaction> getReactions() { return reactions; }
    public boolean isEdited() { return edited; }
    public Instant getEditedAt() { return editedAt; }
    public boolean isDeleted() { return deleted; }
    public Instant getDeletedAt() { return deletedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public record Attachment(
        Long fileId,
        String fileName,
        long fileSize,
        String mimeType,
        String url,
        String thumbnailUrl
    ) {}

    public record SystemEvent(
        String eventType,
        Long targetUserId,
        String targetUserName
    ) {}

    public record ReadReceipt(
        Long userId,
        Instant readAt
    ) {}

    public record Reaction(
        String emoji,
        Long userId,
        Instant createdAt
    ) {}
}
