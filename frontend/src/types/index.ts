// ─── Auth ─────────────────────────────────────────────────────────────────────

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = {
  email: string
  password: string
  name: string
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  user: UserProfile
}

// ─── User ─────────────────────────────────────────────────────────────────────

export type UserProfile = {
  id: number
  email: string
  name: string
  avatarUrl: string | null
  statusMessage: string | null
  online: boolean
}

export type UserSearchResult = {
  id: number
  name: string
  email: string
  avatarUrl: string | null
  online: boolean
}

// ─── Presence ─────────────────────────────────────────────────────────────────

export type PresenceStatus = 'ONLINE' | 'OFFLINE' | 'AWAY'

export type PresenceEvent = {
  userId: number
  status: PresenceStatus
  lastSeenAt: string | null
}

// ─── Chat Room ─────────────────────────────────────────────────────────────────

export type ChatRoomType = 'DIRECT' | 'GROUP'

export type RoomMember = {
  userId: number
  name: string
  avatarUrl: string | null
  online: boolean
  role: 'OWNER' | 'MEMBER'
}

export type LastMessage = {
  content: string
  senderName: string
  sentAt: string
  type: MessageType
}

export type ChatRoom = {
  roomId: number
  type: ChatRoomType
  name: string
  avatarUrl: string | null
  lastMessage: LastMessage | null
  unreadCount: number
  memberCount: number
  members: RoomMember[]
  createdAt: string
}

export type CreateRoomRequest = {
  type: ChatRoomType
  name?: string
  memberIds: number[]
}

// ─── Message ─────────────────────────────────────────────────────────────────

export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM'

export type FileAttachment = {
  fileId: string
  fileName: string
  fileSize: number
  mimeType: string
  url: string
  thumbnailUrl: string | null
}

export type ReadReceipt = {
  userId: number
  userName: string
  readAt: string
}

export type Message = {
  messageId: string
  roomId: number
  senderId: number
  senderName: string
  senderAvatarUrl: string | null
  type: MessageType
  content: string
  attachment: FileAttachment | null
  readBy: ReadReceipt[]
  createdAt: string
  updatedAt: string | null
  deleted: boolean
}

export type ChatMessageRequest = {
  content: string
  type: MessageType
  fileId?: string
}

export type TypingEvent = {
  roomId: number
  userId: number
  userName: string
  typing: boolean
}

export type ReadReceiptEvent = {
  roomId: number
  messageId: string
  userId: number
  userName: string
  readAt: string
}

// ─── Call ─────────────────────────────────────────────────────────────────────

export type CallStatus = 'RINGING' | 'ACTIVE' | 'ENDED' | 'MISSED' | 'REJECTED'

export type CallType = 'VOICE' | 'VIDEO'

export type CallSession = {
  callId: string
  roomId: number
  callType: CallType
  status: CallStatus
  initiatorId: number
  participants: CallParticipant[]
  startedAt: string | null
  endedAt: string | null
}

export type CallParticipant = {
  userId: number
  userName: string
  avatarUrl: string | null
  joined: boolean
  micEnabled: boolean
  cameraEnabled: boolean
}

export type CallStartRequest = {
  roomId: number
  callType: CallType
}

export type SdpOffer = {
  callId: string
  sdp: string
  targetUserId?: number
}

export type SdpAnswer = {
  callId: string
  sdp: string
  fromUserId: number
}

export type IceCandidateMessage = {
  callId: string
  candidate: string
  sdpMid: string | null
  sdpMLineIndex: number | null
  targetUserId?: number
}

export type IncomingCallNotification = {
  callId: string
  roomId: number
  callType: CallType
  initiatorId: number
  initiatorName: string
  initiatorAvatarUrl: string | null
}

// ─── API Response ─────────────────────────────────────────────────────────────

export type ApiResponse<T> = {
  success: boolean
  data: T
}

export type ApiError = {
  success: false
  error: {
    code: string
    message: string
    timestamp: string
  }
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
