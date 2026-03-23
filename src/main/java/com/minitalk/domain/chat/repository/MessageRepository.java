package com.minitalk.domain.chat.repository;

import com.minitalk.domain.chat.document.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    List<Message> findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(
        Long roomId, Instant cursor, Pageable pageable);

    Optional<Message> findTopByRoomIdOrderByCreatedAtDesc(Long roomId);

    long countByRoomIdAndCreatedAtAfter(Long roomId, Instant after);

    long countByRoomId(Long roomId);
}
