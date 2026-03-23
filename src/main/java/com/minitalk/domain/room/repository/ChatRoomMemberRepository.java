package com.minitalk.domain.room.repository;

import com.minitalk.domain.room.entity.ChatRoomMember;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByRoomId(Long roomId);

    List<ChatRoomMember> findByUserId(Long userId);

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    void deleteByRoomIdAndUserId(Long roomId, Long userId);

    int countByRoomId(Long roomId);

    @Modifying
    @Query("UPDATE ChatRoomMember m SET m.lastReadAt = :lastReadAt " +
           "WHERE m.roomId = :roomId AND m.userId = :userId")
    void updateLastReadAt(@Param("roomId") Long roomId,
                          @Param("userId") Long userId,
                          @Param("lastReadAt") LocalDateTime lastReadAt);

    @Query("SELECT m.lastReadAt FROM ChatRoomMember m WHERE m.roomId = :roomId AND m.userId = :userId")
    Optional<LocalDateTime> getLastReadAt(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
