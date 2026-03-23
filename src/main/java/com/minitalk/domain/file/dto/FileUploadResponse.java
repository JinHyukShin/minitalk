package com.minitalk.domain.file.dto;

import com.minitalk.domain.file.entity.FileMetadata;

public record FileUploadResponse(
    Long id,
    String originalName,
    long fileSize,
    String mimeType,
    String downloadUrl,
    String thumbnailUrl
) {

    public static FileUploadResponse from(FileMetadata metadata) {
        String thumbnailUrl = metadata.getThumbnailPath() != null
            ? "/api/v1/files/" + metadata.getId() + "/thumbnail"
            : null;
        return new FileUploadResponse(
            metadata.getId(),
            metadata.getOriginalName(),
            metadata.getFileSize(),
            metadata.getMimeType(),
            "/api/v1/files/" + metadata.getId() + "/download",
            thumbnailUrl
        );
    }
}
