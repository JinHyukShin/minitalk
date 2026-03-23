package com.minitalk.domain.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "stored_path", nullable = false, length = 1000)
    private String storedPath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "thumbnail_path", length = 1000)
    private String thumbnailPath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected FileMetadata() {
    }

    public static FileMetadata create(Long roomId, Long uploaderId, String originalName,
                                       String storedPath, Long fileSize, String mimeType,
                                       String thumbnailPath) {
        FileMetadata metadata = new FileMetadata();
        metadata.roomId = roomId;
        metadata.uploaderId = uploaderId;
        metadata.originalName = originalName;
        metadata.storedPath = storedPath;
        metadata.fileSize = fileSize;
        metadata.mimeType = mimeType;
        metadata.thumbnailPath = thumbnailPath;
        metadata.createdAt = LocalDateTime.now();
        return metadata;
    }

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public Long getUploaderId() { return uploaderId; }
    public String getOriginalName() { return originalName; }
    public String getStoredPath() { return storedPath; }
    public Long getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType; }
    public String getThumbnailPath() { return thumbnailPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
