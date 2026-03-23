package com.minitalk.domain.file.service;

import com.minitalk.domain.chat.service.ChatService;
import com.minitalk.domain.file.dto.FileUploadResponse;
import com.minitalk.domain.file.entity.FileMetadata;
import com.minitalk.domain.file.repository.FileMetadataRepository;
import com.minitalk.domain.room.service.ChatRoomService;
import com.minitalk.global.exception.BusinessException;
import com.minitalk.global.exception.ErrorCode;
import com.minitalk.infra.minio.MinioService;
import com.minitalk.infra.redis.RedisPublisher;
import com.minitalk.domain.chat.dto.ChatMessageResponse;
import com.minitalk.domain.chat.document.Message;
import java.io.InputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final MinioService minioService;
    private final FileMetadataRepository metadataRepository;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final RedisPublisher redisPublisher;

    public FileService(MinioService minioService,
                       FileMetadataRepository metadataRepository,
                       ChatService chatService,
                       ChatRoomService chatRoomService,
                       RedisPublisher redisPublisher) {
        this.minioService = minioService;
        this.metadataRepository = metadataRepository;
        this.chatService = chatService;
        this.chatRoomService = chatRoomService;
        this.redisPublisher = redisPublisher;
    }

    @Transactional
    public FileUploadResponse uploadAndSendMessage(Long roomId, Long userId, MultipartFile file) {
        chatRoomService.validateMembership(roomId, userId);

        String storedPath = minioService.upload(file, "chat-files/" + roomId);

        String thumbnailPath = null;
        if (isImage(file.getContentType())) {
            thumbnailPath = minioService.upload(file, "chat-thumbnails/" + roomId);
        }

        FileMetadata metadata = FileMetadata.create(
            roomId, userId, file.getOriginalFilename(),
            storedPath, file.getSize(), file.getContentType(), thumbnailPath);
        metadataRepository.save(metadata);

        String messageType = isImage(file.getContentType()) ? "IMAGE" : "FILE";
        String downloadUrl = "/api/v1/files/" + metadata.getId() + "/download";
        String thumbUrl = thumbnailPath != null
            ? "/api/v1/files/" + metadata.getId() + "/thumbnail"
            : null;

        Message message = chatService.saveAndBroadcastFileMessage(
            roomId, userId, metadata.getId(), file.getOriginalFilename(),
            file.getSize(), file.getContentType(), downloadUrl, thumbUrl, messageType);

        ChatMessageResponse response = ChatMessageResponse.from(message);
        redisPublisher.publish("channel:room:" + roomId, response);

        return FileUploadResponse.from(metadata);
    }

    public InputStream download(Long fileId, Long userId) {
        FileMetadata metadata = metadataRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        chatRoomService.validateMembership(metadata.getRoomId(), userId);

        return minioService.download(metadata.getStoredPath());
    }

    public FileMetadata getMetadata(Long fileId) {
        return metadataRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    private boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
