package com.minitalk.domain.file.controller;

import com.minitalk.domain.file.dto.FileUploadResponse;
import com.minitalk.domain.file.entity.FileMetadata;
import com.minitalk.domain.file.service.FileService;
import com.minitalk.global.common.ApiResponse;
import com.minitalk.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File", description = "파일 API")
@RestController
@RequestMapping("/api/v1")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "파일 업로드")
    @PostMapping("/rooms/{roomId}/files")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long roomId,
        @RequestParam("file") MultipartFile file
    ) {
        FileUploadResponse response = fileService.uploadAndSendMessage(roomId, userDetails.id(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "파일 다운로드")
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long fileId
    ) {
        FileMetadata metadata = fileService.getMetadata(fileId);
        InputStream inputStream = fileService.download(fileId, userDetails.id());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + metadata.getOriginalName() + "\"")
            .contentType(MediaType.parseMediaType(metadata.getMimeType()))
            .body(new InputStreamResource(inputStream));
    }
}
