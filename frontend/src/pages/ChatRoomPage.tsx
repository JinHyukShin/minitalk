import {
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react'
import { MessageBubble } from '../components/chat/MessageBubble'
import { MessageInput } from '../components/chat/MessageInput'
import { TypingIndicator } from '../components/chat/TypingIndicator'
import { UserAvatar } from '../components/user/UserAvatar'
import { messageApi, fileApi, callApi } from '../api/client'
import type {
  ChatRoom,
  Message,
  PresenceStatus,
  TypingEvent,
  ReadReceiptEvent,
  UserProfile,
  CallSession,
  CallStartRequest,
} from '../types'

type ChatRoomPageProps = {
  room: ChatRoom
  currentUser: UserProfile
  onlineSatuses: Record<number, PresenceStatus>
  onSendMessage: (roomId: number, content: string) => void
  onSendTyping: (roomId: number, typing: boolean) => void
  onSendReadReceipt: (roomId: number, messageId: string) => void
  typingEvents: TypingEvent[]
  readReceiptEvents: ReadReceiptEvent[]
  newMessages: Message[]
  onCallStart: (session: CallSession) => void
}

export function ChatRoomPage({
  room,
  currentUser,
  onlineSatuses,
  onSendMessage,
  onSendTyping,
  onSendReadReceipt,
  typingEvents,
  readReceiptEvents,
  newMessages,
  onCallStart,
}: ChatRoomPageProps) {
  const [messages, setMessages] = useState<Message[]>([])
  const [loading, setLoading] = useState(false)
  const [hasMore, setHasMore] = useState(true)
  const [cursor, setCursor] = useState<string | undefined>(undefined)
  const bottomRef = useRef<HTMLDivElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const prevNewMessagesLength = useRef(0)

  // 초기 메시지 로드
  useEffect(() => {
    setMessages([])
    setCursor(undefined)
    setHasMore(true)
    prevNewMessagesLength.current = 0

    const loadInitial = async () => {
      setLoading(true)
      try {
        const res = await messageApi.history(room.roomId, undefined, 30)
        const page = res.data.data
        setMessages(page.content.reverse())
        setHasMore(page.page < page.totalPages - 1)
        if (page.content.length > 0) {
          setCursor(page.content[page.content.length - 1]?.messageId)
        }
      } catch {
        // ignore
      } finally {
        setLoading(false)
      }
    }
    loadInitial()
  }, [room.roomId])

  // 실시간 새 메시지 추가
  useEffect(() => {
    if (newMessages.length === prevNewMessagesLength.current) return
    const added = newMessages.slice(prevNewMessagesLength.current)
    prevNewMessagesLength.current = newMessages.length
    setMessages((prev) => {
      const existingIds = new Set(prev.map((m) => m.messageId))
      const fresh = added.filter((m) => !existingIds.has(m.messageId))
      return [...prev, ...fresh]
    })
  }, [newMessages])

  // 새 메시지 도착 시 스크롤 하단
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages.length])

  // 읽음 확인 전송 (가장 마지막 메시지)
  useEffect(() => {
    const last = messages[messages.length - 1]
    if (last && last.senderId !== currentUser.id) {
      onSendReadReceipt(room.roomId, last.messageId)
    }
  }, [messages, currentUser.id, room.roomId, onSendReadReceipt])

  // 실시간 읽음 확인 적용: readReceiptEvents -> 메시지 readBy 업데이트
  useEffect(() => {
    if (readReceiptEvents.length === 0) return
    const recent = readReceiptEvents[readReceiptEvents.length - 1]
    if (!recent || recent.roomId !== room.roomId) return
    setMessages((prev) =>
      prev.map((msg) => {
        if (msg.messageId !== recent.messageId) return msg
        const alreadyRead = msg.readBy.some((r) => r.userId === recent.userId)
        if (alreadyRead) return msg
        return {
          ...msg,
          readBy: [
            ...msg.readBy,
            { userId: recent.userId, userName: recent.userName, readAt: recent.readAt },
          ],
        }
      })
    )
  }, [readReceiptEvents, room.roomId])

  // 이전 메시지 불러오기
  const handleLoadMore = useCallback(async () => {
    if (loading || !hasMore) return
    const scrollEl = containerRef.current
    const prevScrollHeight = scrollEl?.scrollHeight ?? 0

    setLoading(true)
    try {
      const res = await messageApi.history(room.roomId, cursor, 30)
      const page = res.data.data
      const older = page.content.reverse()
      setMessages((prev) => [...older, ...prev])
      setHasMore(page.page < page.totalPages - 1)
      if (older.length > 0) {
        setCursor(older[0]?.messageId)
      }
      // 스크롤 위치 유지
      requestAnimationFrame(() => {
        if (scrollEl) {
          scrollEl.scrollTop = scrollEl.scrollHeight - prevScrollHeight
        }
      })
    } catch {
      // ignore
    } finally {
      setLoading(false)
    }
  }, [loading, hasMore, room.roomId, cursor])

  const handleScroll = useCallback(() => {
    const el = containerRef.current
    if (el && el.scrollTop < 80) {
      handleLoadMore()
    }
  }, [handleLoadMore])

  const handleSend = useCallback(
    (content: string) => {
      onSendMessage(room.roomId, content)
    },
    [room.roomId, onSendMessage]
  )

  const handleFileSelect = useCallback(
    async (file: File) => {
      try {
        const res = await fileApi.upload(room.roomId, file)
        const attachment = res.data.data
        const isImage = file.type.startsWith('image/')
        onSendMessage(room.roomId, isImage ? '사진을 공유했습니다.' : file.name)
        // 실제 구현에서는 fileId를 포함한 메시지 전송
        console.log('Uploaded file:', attachment)
      } catch {
        alert('파일 업로드에 실패했습니다.')
      }
    },
    [room.roomId, onSendMessage]
  )

  const handleStartCall = useCallback(
    async (callType: 'VOICE' | 'VIDEO') => {
      try {
        const req: CallStartRequest = { roomId: room.roomId, callType }
        const res = await callApi.start(req)
        onCallStart(res.data.data)
      } catch {
        alert('통화 시작에 실패했습니다.')
      }
    },
    [room.roomId, onCallStart]
  )

  // 타이핑 중인 사람들
  const roomTypers = typingEvents
    .filter((e) => e.roomId === room.roomId && e.userId !== currentUser.id && e.typing)
    .map((e) => e.userName)

  // 연속된 같은 사람의 메시지에서 아바타 표시 여부
  const shouldShowAvatar = (idx: number): boolean => {
    if (idx === 0) return true
    const prev = messages[idx - 1]
    const curr = messages[idx]
    if (!prev || !curr) return true
    return prev.senderId !== curr.senderId || prev.type === 'SYSTEM'
  }

  // 날짜 구분선
  const getDividerDate = (idx: number): string | null => {
    if (idx === 0) return messages[0] ? formatDateDivider(messages[0].createdAt) : null
    const prev = messages[idx - 1]
    const curr = messages[idx]
    if (!prev || !curr) return null
    const prevDate = new Date(prev.createdAt).toDateString()
    const currDate = new Date(curr.createdAt).toDateString()
    return prevDate !== currDate ? formatDateDivider(curr.createdAt) : null
  }

  return (
    <div className="flex flex-col h-full bg-gray-900">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 bg-gray-800 border-b border-gray-700 flex-shrink-0">
        <div className="flex items-center gap-3 min-w-0">
          {room.type === 'GROUP' ? (
            <div className="w-9 h-9 bg-indigo-600 rounded-full flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
                <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
              </svg>
            </div>
          ) : (
            <UserAvatar
              name={room.members[0]?.name ?? room.name}
              avatarUrl={room.members[0]?.avatarUrl ?? null}
              status={
                room.members[0]
                  ? (onlineSatuses[room.members[0].userId] ?? 'OFFLINE')
                  : undefined
              }
              size="sm"
            />
          )}
          <div className="min-w-0">
            <h2 className="text-sm font-semibold text-white truncate">{room.name}</h2>
            <p className="text-xs text-gray-400">
              {room.type === 'GROUP' ? `멤버 ${room.memberCount}명` : ''}
            </p>
          </div>
        </div>

        {/* Call buttons */}
        <div className="flex items-center gap-1">
          <button
            onClick={() => handleStartCall('VOICE')}
            className="p-2 text-gray-400 hover:text-white hover:bg-gray-700 rounded-lg transition-colors"
            title="음성 통화"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
            </svg>
          </button>
          <button
            onClick={() => handleStartCall('VIDEO')}
            className="p-2 text-gray-400 hover:text-white hover:bg-gray-700 rounded-lg transition-colors"
            title="화상 통화"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.069A1 1 0 0121 8.87v6.26a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
          </button>
        </div>
      </div>

      {/* Messages */}
      <div
        ref={containerRef}
        className="flex-1 overflow-y-auto px-4 py-4 space-y-1"
        onScroll={handleScroll}
      >
        {loading && (
          <div className="flex justify-center py-4">
            <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
          </div>
        )}

        {messages.map((msg, idx) => {
          const divider = getDividerDate(idx)
          return (
            <div key={msg.messageId}>
              {divider && (
                <div className="flex items-center justify-center my-4">
                  <span className="text-xs text-gray-500 bg-gray-800 px-3 py-1 rounded-full">
                    {divider}
                  </span>
                </div>
              )}
              <MessageBubble
                message={msg}
                currentUserId={currentUser.id}
                showAvatar={shouldShowAvatar(idx)}
              />
            </div>
          )
        })}

        <TypingIndicator typers={roomTypers} />
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <MessageInput
        onSend={handleSend}
        onFileSelect={handleFileSelect}
        onTypingStart={() => onSendTyping(room.roomId, true)}
        onTypingStop={() => onSendTyping(room.roomId, false)}
      />
    </div>
  )
}

function formatDateDivider(iso: string): string {
  const date = new Date(iso)
  const today = new Date()
  const diff = Math.floor((today.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))
  if (diff === 0) return '오늘'
  if (diff === 1) return '어제'
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`
}
