package com.minitalk.domain.room.repository;

import com.minitalk.domain.room.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr JOIN ChatRoomMember crm ON cr.id = crm.roomId " +
           "WHERE crm.userId = :userId ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.type = 'DIRECT' AND cr.id IN " +
           "(SELECT crm1.roomId FROM ChatRoomMember crm1 WHERE crm1.userId = :userId1) " +
           "AND cr.id IN " +
           "(SELECT crm2.roomId FROM ChatRoomMember crm2 WHERE crm2.userId = :userId2)")
    List<ChatRoom> findDirectRoomBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
