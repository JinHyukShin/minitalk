package com.minitalk.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Common
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_INVALID_INPUT", "잘못된 입력입니다."),
    COMMON_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_UNAUTHORIZED", "인증이 필요합니다."),
    COMMON_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_FORBIDDEN", "권한이 없습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_NOT_FOUND", "리소스를 찾을 수 없습니다."),
    COMMON_DUPLICATE(HttpStatus.CONFLICT, "COMMON_DUPLICATE", "이미 존재하는 리소스입니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_ERROR", "서버 오류가 발생했습니다."),

    // Auth
    AUTH_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "AUTH_EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_INVALID", "유효하지 않은 토큰입니다."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_TOKEN_NOT_FOUND", "리프레시 토큰을 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    ROOM_NOT_MEMBER(HttpStatus.FORBIDDEN, "ROOM_NOT_MEMBER", "채팅방 멤버가 아닙니다."),
    ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "ROOM_ALREADY_EXISTS", "이미 존재하는 1:1 채팅방입니다."),
    ROOM_DIRECT_MEMBER_LIMIT(HttpStatus.BAD_REQUEST, "ROOM_DIRECT_MEMBER_LIMIT", "1:1 채팅방은 2명만 참여할 수 있습니다."),

    // Chat
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다."),
    CHAT_NOT_MESSAGE_OWNER(HttpStatus.FORBIDDEN, "CHAT_NOT_MESSAGE_OWNER", "본인 메시지만 수정/삭제할 수 있습니다."),

    // Call
    CALL_NOT_FOUND(HttpStatus.NOT_FOUND, "CALL_NOT_FOUND", "통화를 찾을 수 없습니다."),
    CALL_ALREADY_ACTIVE(HttpStatus.CONFLICT, "CALL_ALREADY_ACTIVE", "이미 진행 중인 통화가 있습니다."),
    CALL_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "CALL_MAX_PARTICIPANTS", "최대 참가자 수를 초과했습니다."),
    CALL_INVALID_STATUS(HttpStatus.BAD_REQUEST, "CALL_INVALID_STATUS", "유효하지 않은 통화 상태입니다."),

    // File
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "파일 크기가 제한을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
