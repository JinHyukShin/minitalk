-- 사용자
CREATE TABLE IF NOT EXISTS app_user (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    avatar_url      VARCHAR(500),
    status_message  VARCHAR(200),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 리프레시 토큰
CREATE TABLE IF NOT EXISTS refresh_token (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 채팅방
CREATE TABLE IF NOT EXISTS chat_room (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(10)  NOT NULL,
    name            VARCHAR(200),
    icon_url        VARCHAR(500),
    created_by      BIGINT       REFERENCES app_user(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 채팅방 멤버
CREATE TABLE IF NOT EXISTS chat_room_member (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT       NOT NULL REFERENCES chat_room(id) ON DELETE CASCADE,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role            VARCHAR(10)  NOT NULL DEFAULT 'MEMBER',
    nickname        VARCHAR(100),
    joined_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_read_at    TIMESTAMP,
    UNIQUE (room_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_room_member_room ON chat_room_member (room_id);
CREATE INDEX IF NOT EXISTS idx_room_member_user ON chat_room_member (user_id);

-- 파일 메타데이터
CREATE TABLE IF NOT EXISTS file_metadata (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT       NOT NULL REFERENCES chat_room(id),
    uploader_id     BIGINT       NOT NULL REFERENCES app_user(id),
    original_name   VARCHAR(500) NOT NULL,
    stored_path     VARCHAR(1000) NOT NULL,
    file_size       BIGINT       NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    thumbnail_path  VARCHAR(1000),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_file_room ON file_metadata (room_id, created_at DESC);

-- 통화 이력
CREATE TABLE IF NOT EXISTS call_history (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT       NOT NULL REFERENCES chat_room(id),
    caller_id       BIGINT       NOT NULL REFERENCES app_user(id),
    call_type       VARCHAR(10)  NOT NULL,
    status          VARCHAR(10)  NOT NULL,
    started_at      TIMESTAMP,
    ended_at        TIMESTAMP,
    duration_seconds INT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_call_room ON call_history (room_id, created_at DESC);

-- 통화 참가자
CREATE TABLE IF NOT EXISTS call_participant (
    id              BIGSERIAL PRIMARY KEY,
    call_id         BIGINT       NOT NULL REFERENCES call_history(id) ON DELETE CASCADE,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id),
    joined_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    left_at         TIMESTAMP,
    UNIQUE (call_id, user_id)
);
