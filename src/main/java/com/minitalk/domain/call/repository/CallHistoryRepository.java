package com.minitalk.domain.call.repository;

import com.minitalk.domain.call.entity.CallHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallHistoryRepository extends JpaRepository<CallHistory, Long> {

    List<CallHistory> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}
