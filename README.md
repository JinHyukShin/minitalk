# MiniTalk - 실시간 채팅 & 화상통화 플랫폼

WebSocket(STOMP) 기반 실시간 채팅 + WebRTC 화상통화 + Redis Pub/Sub 다중 인스턴스 스케일링 플랫폼

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java (OpenJDK 25) |
| Framework | Spring Boot 3.4.4 |
| WebSocket | STOMP over WebSocket |
| WebRTC | WebRTC + Kurento SFU |
| 관계형 DB | PostgreSQL 17 |
| 문서 DB | MongoDB 7 |
| Cache/Pub-Sub | Redis 7.4 |
| 파일 저장소 | MinIO (S3 호환) |
| Auth | Spring Security + JWT |
| Build | Gradle (Kotlin DSL) |
| Test | JUnit 5, Testcontainers |
| Infra | Docker, Docker Compose |

## 아키텍처

```
React Frontend (STOMP + WebRTC + REST)
          |
    Spring Boot (N instances)
    ├── STOMP Controller (chat, typing, read)
    ├── Signaling Controller (SDP, ICE)
    ├── Redis Pub/Sub (multi-instance broadcast)
    └── Presence Service (Redis Hash + TTL)
          |
    ┌─────┼─────┬──────┐
PostgreSQL  MongoDB  Redis  MinIO
(users,     (msgs)   (state) (files)
 rooms)
```

## 주요 기능

- **실시간 1:1/그룹 채팅**: WebSocket STOMP 기반 메시지 송수신
- **읽음 확인**: MongoDB readBy 배열 + 미읽음 수 계산
- **타이핑 표시**: Redis TTL 3초 자동 만료
- **프레즌스**: Redis Hash + TTL 300초 + 하트비트
- **파일 공유**: MinIO 업로드/다운로드 + 이미지 썸네일
- **화상/음성 통화**: WebRTC + Kurento SFU (시그널링 흐름)
- **다중 인스턴스 스케일링**: Redis Pub/Sub 메시지 브로드캐스트
- **JWT 인증**: Access/Refresh 토큰 + WebSocket STOMP 인증

## API 엔드포인트

### REST API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/v1/auth/signup | 회원가입 |
| POST | /api/v1/auth/login | 로그인 |
| POST | /api/v1/auth/refresh | 토큰 갱신 |
| POST | /api/v1/auth/logout | 로그아웃 |
| GET | /api/v1/users/me | 내 프로필 |
| PUT | /api/v1/users/me | 프로필 수정 |
| GET | /api/v1/users/search?q= | 사용자 검색 |
| POST | /api/v1/rooms | 채팅방 생성 |
| GET | /api/v1/rooms | 내 채팅방 목록 |
| GET | /api/v1/rooms/{roomId} | 채팅방 상세 |
| GET | /api/v1/rooms/{roomId}/messages | 메시지 히스토리 |
| POST | /api/v1/rooms/{roomId}/files | 파일 업로드 |
| POST | /api/v1/calls/start | 통화 시작 |
| GET | /api/v1/presence/online | 온라인 사용자 |

### WebSocket STOMP 구독 경로

| 방향 | 경로 | 설명 |
|------|------|------|
| 구독 (SUBSCRIBE) | /topic/room/{roomId} | 채팅방 메시지 |
| 구독 (SUBSCRIBE) | /topic/room/{roomId}/typing | 타이핑 표시 |
| 구독 (SUBSCRIBE) | /topic/room/{roomId}/read | 읽음 확인 |
| 구독 (SUBSCRIBE) | /user/queue/notifications | 개인 알림 |
| 구독 (SUBSCRIBE) | /user/queue/signal | WebRTC 시그널링 |
| 전송 (SEND) | /app/chat/{roomId} | 메시지 전송 |
| 전송 (SEND) | /app/chat/{roomId}/typing | 타이핑 시작/중지 |
| 전송 (SEND) | /app/chat/{roomId}/read | 읽음 확인 |
| 전송 (SEND) | /app/signal/sdp-offer | SDP Offer |
| 전송 (SEND) | /app/signal/ice-candidate | ICE Candidate |

## 실행 방법

### 1. 인프라 시작

```bash
cd scripts
bash start-infra.sh
```

### 2. 애플리케이션 실행

```bash
bash scripts/run.sh
```

### 3. Docker 전체 실행

```bash
cd docker
cp .env.example .env
docker compose up -d
```

### 4. API 문서

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
