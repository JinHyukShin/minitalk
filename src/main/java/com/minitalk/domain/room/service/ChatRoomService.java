package com.minitalk.domain.room.service;

import com.minitalk.domain.auth.entity.User;
import com.minitalk.domain.auth.repository.UserRepository;
import com.minitalk.domain.chat.document.Message;
import com.minitalk.domain.chat.repository.MessageRepository;
import com.minitalk.domain.presence.service.PresenceService;
import com.minitalk.domain.room.dto.AddMemberRequest;
import com.minitalk.domain.room.dto.ChatRoomCreateRequest;
import com.minitalk.domain.room.dto.ChatRoomListResponse;
import com.minitalk.domain.room.dto.ChatRoomResponse;
import com.minitalk.domain.room.dto.ChatRoomUpdateRequest;
import com.minitalk.domain.room.entity.ChatRoom;
import com.minitalk.domain.room.entity.ChatRoomMember;
import com.minitalk.domain.room.entity.ChatRoomType;
import com.minitalk.domain.room.repository.ChatRoomMemberRepository;
import com.minitalk.domain.room.repository.ChatRoomRepository;
import com.minitalk.global.exception.BusinessException;
import com.minitalk.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PresenceService presenceService;

    public ChatRoomService(ChatRoomRepository chatRoomRepository,
                           ChatRoomMemberRepository memberRepository,
                           UserRepository userRepository,
                           MessageRepository messageRepository,
                           PresenceService presenceService) {
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.presenceService = presenceService;
    }

    @Transactional
    public ChatRoomResponse createRoom(Long userId, ChatRoomCreateRequest request) {
        ChatRoomType type = ChatRoomType.valueOf(request.type());

        if (type == ChatRoomType.DIRECT) {
            if (request.memberIds().size() != 1) {
                throw new BusinessException(ErrorCode.ROOM_DIRECT_MEMBER_LIMIT);
            }

            Long otherUserId = request.memberIds().getFirst();
            List<ChatRoom> existing = chatRoomRepository.findDirectRoomBetween(userId, otherUserId);
            if (!existing.isEmpty()) {
                ChatRoom existingRoom = existing.getFirst();
                return buildChatRoomResponse(existingRoom);
            }

            ChatRoom room = ChatRoom.createDirect(userId);
            chatRoomRepository.save(room);

            memberRepository.save(ChatRoomMember.create(room.getId(), userId, "ADMIN"));
            memberRepository.save(ChatRoomMember.create(room.getId(), otherUserId, "MEMBER"));

            return buildChatRoomResponse(room);
        }

        ChatRoom room = ChatRoom.createGroup(request.name(), userId);
        chatRoomRepository.save(room);

        memberRepository.save(ChatRoomMember.create(room.getId(), userId, "ADMIN"));
        for (Long memberId : request.memberIds()) {
            if (!memberId.equals(userId)) {
                memberRepository.save(ChatRoomMember.create(room.getId(), memberId, "MEMBER"));
            }
        }

        return buildChatRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getMyRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findAllByUserId(userId);
        List<ChatRoomListResponse> responses = new ArrayList<>();

        for (ChatRoom room : rooms) {
            Optional<Message> lastMsg = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getId());
            int memberCount = memberRepository.countByRoomId(room.getId());

            ChatRoomListResponse.LastMessageInfo lastMessageInfo = null;
            if (lastMsg.isPresent()) {
                Message msg = lastMsg.get();
                lastMessageInfo = new ChatRoomListResponse.LastMessageInfo(
                    msg.getContent(),
                    msg.getSenderName(),
                    msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null,
                    msg.getType()
                );
            }

            Optional<LocalDateTime> lastReadAt = memberRepository.getLastReadAt(room.getId(), userId);
            int unreadCount = 0;
            if (lastReadAt.isPresent()) {
                unreadCount = (int) messageRepository.countByRoomIdAndCreatedAtAfter(
                    room.getId(),
                    lastReadAt.get().atZone(ZoneId.systemDefault()).toInstant());
            }

            String roomName = room.getName();
            if (room.getType() == ChatRoomType.DIRECT && roomName == null) {
                List<ChatRoomMember> members = memberRepository.findByRoomId(room.getId());
                for (ChatRoomMember member : members) {
                    if (!member.getUserId().equals(userId)) {
                        User otherUser = userRepository.findById(member.getUserId()).orElse(null);
                        if (otherUser != null) {
                            roomName = otherUser.getName();
                        }
                    }
                }
            }

            responses.add(new ChatRoomListResponse(
                room.getId(),
                room.getType().name(),
                roomName,
                lastMessageInfo,
                unreadCount,
                memberCount
            ));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long roomId, Long userId) {
        validateMembership(roomId, userId);
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return buildChatRoomResponse(room);
    }

    @Transactional
    public ChatRoomResponse updateRoom(Long roomId, Long userId, ChatRoomUpdateRequest request) {
        validateMembership(roomId, userId);
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        room.updateSettings(request.name(), request.iconUrl());
        return buildChatRoomResponse(room);
    }

    @Transactional
    public void addMembers(Long roomId, Long userId, AddMemberRequest request) {
        validateMembership(roomId, userId);
        for (Long memberId : request.userIds()) {
            if (!memberRepository.existsByRoomIdAndUserId(roomId, memberId)) {
                memberRepository.save(ChatRoomMember.create(roomId, memberId, "MEMBER"));
            }
        }
    }

    @Transactional
    public void removeMember(Long roomId, Long userId, Long targetUserId) {
        validateMembership(roomId, userId);
        memberRepository.deleteByRoomIdAndUserId(roomId, targetUserId);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        validateMembership(roomId, userId);
        memberRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    public void validateMembership(Long roomId, Long userId) {
        if (!memberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(ErrorCode.ROOM_NOT_MEMBER);
        }
    }

    private ChatRoomResponse buildChatRoomResponse(ChatRoom room) {
        List<ChatRoomMember> members = memberRepository.findByRoomId(room.getId());
        List<ChatRoomResponse.MemberInfo> memberInfos = new ArrayList<>();

        for (ChatRoomMember member : members) {
            User user = userRepository.findById(member.getUserId()).orElse(null);
            if (user != null) {
                boolean online = presenceService.isOnline(member.getUserId());
                memberInfos.add(new ChatRoomResponse.MemberInfo(
                    user.getId(), user.getName(), user.getAvatarUrl(), online));
            }
        }

        return ChatRoomResponse.from(room, memberInfos);
    }
}
