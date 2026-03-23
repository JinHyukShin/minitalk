package com.minitalk.domain.room.entity;

import com.minitalk.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChatRoomType type;

    @Column(length = 200)
    private String name;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "created_by")
    private Long createdBy;

    protected ChatRoom() {
    }

    public static ChatRoom createDirect(Long createdBy) {
        ChatRoom room = new ChatRoom();
        room.type = ChatRoomType.DIRECT;
        room.createdBy = createdBy;
        return room;
    }

    public static ChatRoom createGroup(String name, Long createdBy) {
        ChatRoom room = new ChatRoom();
        room.type = ChatRoomType.GROUP;
        room.name = name;
        room.createdBy = createdBy;
        return room;
    }

    public void updateSettings(String name, String iconUrl) {
        if (name != null) {
            this.name = name;
        }
        if (iconUrl != null) {
            this.iconUrl = iconUrl;
        }
    }

    public Long getId() {
        return id;
    }

    public ChatRoomType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
