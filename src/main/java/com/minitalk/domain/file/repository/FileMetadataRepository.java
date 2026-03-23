package com.minitalk.domain.file.repository;

import com.minitalk.domain.file.entity.FileMetadata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}
