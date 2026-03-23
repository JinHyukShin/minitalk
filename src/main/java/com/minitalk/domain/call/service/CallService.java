package com.minitalk.domain.call.service;

import com.minitalk.domain.call.dto.CallResponse;
import com.minitalk.domain.call.dto.CallStartRequest;
import com.minitalk.domain.call.entity.CallHistory;
import com.minitalk.domain.call.entity.CallParticipant;
import com.minitalk.domain.call.entity.CallStatus;
import com.minitalk.domain.call.manager.CallSessionManager;
import com.minitalk.domain.call.repository.CallHistoryRepository;
import com.minitalk.domain.call.repository.CallParticipantRepository;
import com.minitalk.domain.room.repository.ChatRoomMemberRepository;
import com.minitalk.domain.room.entity.ChatRoomMember;
import com.minitalk.global.exception.BusinessException;
import com.minitalk.global.exception.ErrorCode;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CallService {

    private static final int MAX_GROUP_CALL_PARTICIPANTS = 6;

    private final CallHistoryRepository callHistoryRepository;
    private final CallParticipantRepository participantRepository;
    private final CallSessionManager sessionManager;
    private final ChatRoomMemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public CallService(CallHistoryRepository callHistoryRepository,
                       CallParticipantRepository participantRepository,
                       CallSessionManager sessionManager,
                       ChatRoomMemberRepository memberRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.callHistoryRepository = callHistoryRepository;
        this.participantRepository = participantRepository;
        this.sessionManager = sessionManager;
        this.memberRepository = memberRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public CallResponse startCall(Long callerId, CallStartRequest request) {
        CallHistory call = CallHistory.create(request.roomId(), callerId, request.callType());
        callHistoryRepository.save(call);

        CallParticipant caller = CallParticipant.create(call.getId(), callerId);
        participantRepository.save(caller);

        sessionManager.createSession(call.getId(), callerId);
        sessionManager.getSession(call.getId()).addParticipant(callerId);

        List<ChatRoomMember> members = memberRepository.findByRoomId(request.roomId());
        for (ChatRoomMember member : members) {
            if (!member.getUserId().equals(callerId)) {
                messagingTemplate.convertAndSendToUser(
                    String.valueOf(member.getUserId()),
                    "/queue/notifications",
                    CallResponse.from(call));
            }
        }

        return CallResponse.from(call);
    }

    @Transactional
    public CallResponse acceptCall(Long callId, Long userId) {
        CallHistory call = findCall(callId);
        if (call.getStatus() != CallStatus.RINGING) {
            throw new BusinessException(ErrorCode.CALL_INVALID_STATUS);
        }

        call.accept();

        CallParticipant participant = CallParticipant.create(callId, userId);
        participantRepository.save(participant);

        if (sessionManager.hasSession(callId)) {
            sessionManager.getSession(callId).addParticipant(userId);
        }

        return CallResponse.from(call);
    }

    @Transactional
    public CallResponse rejectCall(Long callId, Long userId) {
        CallHistory call = findCall(callId);
        if (call.getStatus() != CallStatus.RINGING) {
            throw new BusinessException(ErrorCode.CALL_INVALID_STATUS);
        }

        call.reject();
        sessionManager.removeSession(callId);

        return CallResponse.from(call);
    }

    @Transactional
    public CallResponse endCall(Long callId, Long userId) {
        CallHistory call = findCall(callId);
        call.end();
        sessionManager.removeSession(callId);

        return CallResponse.from(call);
    }

    @Transactional
    public CallResponse joinGroupCall(Long callId, Long userId) {
        CallHistory call = findCall(callId);
        if (call.getStatus() != CallStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CALL_INVALID_STATUS);
        }

        int currentCount = participantRepository.countByCallIdAndLeftAtIsNull(callId);
        if (currentCount >= MAX_GROUP_CALL_PARTICIPANTS) {
            throw new BusinessException(ErrorCode.CALL_MAX_PARTICIPANTS);
        }

        CallParticipant participant = CallParticipant.create(callId, userId);
        participantRepository.save(participant);

        if (sessionManager.hasSession(callId)) {
            sessionManager.getSession(callId).addParticipant(userId);
        }

        return CallResponse.from(call);
    }

    @Transactional(readOnly = true)
    public List<CallResponse> getCallHistory(Long roomId) {
        return callHistoryRepository.findByRoomIdOrderByCreatedAtDesc(roomId)
            .stream().map(CallResponse::from).toList();
    }

    private CallHistory findCall(Long callId) {
        return callHistoryRepository.findById(callId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_NOT_FOUND));
    }
}
