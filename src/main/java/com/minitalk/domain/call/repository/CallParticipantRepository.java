package com.minitalk.domain.call.repository;

import com.minitalk.domain.call.entity.CallParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallParticipantRepository extends JpaRepository<CallParticipant, Long> {

    List<CallParticipant> findByCallId(Long callId);

    Optional<CallParticipant> findByCallIdAndUserId(Long callId, Long userId);

    int countByCallIdAndLeftAtIsNull(Long callId);
}
