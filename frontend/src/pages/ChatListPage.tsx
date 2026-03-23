import { useCallback, useEffect, useState } from 'react'
import { RoomListItem } from '../components/room/RoomListItem'
import { CreateRoomModal } from '../components/room/CreateRoomModal'
import { UserAvatar } from '../components/user/UserAvatar'
import { ChatRoomPage } from './ChatRoomPage'
import { ProfilePage } from './ProfilePage'
import { VideoCallPage } from './VideoCallPage'
import { roomApi, callApi } from '../api/client'
import { useSTOMP } from '../hooks/useSTOMP'
import { usePresence, useHeartbeat } from '../hooks/usePresence'
import type {
  ChatRoom,
  Message,
  TypingEvent,
  ReadReceiptEvent,
  PresenceEvent,
  UserProfile,
  CallSession,
  IncomingCallNotification,
  ChatMessageRequest,
} from '../types'

type ChatListPageProps = {
  currentUser: UserProfile
  onLogout: () => void
}

export function ChatListPage({ currentUser, onLogout }: ChatListPageProps) {
  const [rooms, setRooms] = useState<ChatRoom[]>([])
  const [activeRoomId, setActiveRoomId] = useState<number | null>(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showProfile, setShowProfile] = useState(false)
  const [user, setUser] = useState(currentUser)

  // 메시지 버퍼: roomId -> Message[]
  const [messageBuffers, setMessageBuffers] = useState<Record<number, Message[]>>({})
  const [typingEvents, setTypingEvents] = useState<TypingEvent[]>([])
  const [readReceiptEvents, setReadReceiptEvents] = useState<ReadReceiptEvent[]>([])
  const [activeCall, setActiveCall] = useState<CallSession | null>(null)
  const [incomingCall, setIncomingCall] = useState<IncomingCallNotification | null>(null)

  const { presenceMap, updatePresence } = usePresence()

  // STOMP 연결
  const stomp = useSTOMP({
    onMessage: useCallback((msg: Message) => {
      setMessageBuffers((prev) => ({
        ...prev,
        [msg.roomId]: [...(prev[msg.roomId] ?? []), msg],
      }))
      // 채팅방 목록 마지막 메시지 업데이트
      setRooms((prev) =>
        prev.map((room) => {
          if (room.roomId !== msg.roomId) return room
          const unreadDelta = msg.senderId !== currentUser.id ? 1 : 0
          return {
            ...room,
            lastMessage: {
              content: msg.content,
              senderName: msg.senderName,
              sentAt: msg.createdAt,
              type: msg.type,
            },
            unreadCount: room.roomId === activeRoomId
              ? 0
              : room.unreadCount + unreadDelta,
          }
        })
      )
    }, [currentUser.id, activeRoomId]),

    onTyping: useCallback((event: TypingEvent) => {
      setTypingEvents((prev) => {
        const filtered = prev.filter(
          (e) => !(e.roomId === event.roomId && e.userId === event.userId)
        )
        return event.typing ? [...filtered, event] : filtered
      })
    }, []),

    onReadReceipt: useCallback((event: ReadReceiptEvent) => {
      setReadReceiptEvents((prev) => [...prev.slice(-200), event])
    }, []),

    onPresence: useCallback((event: PresenceEvent) => {
      updatePresence(event.userId, event.status)
    }, [updatePresence]),

    onIncomingCall: useCallback((notification: IncomingCallNotification) => {
      setIncomingCall(notification)
    }, []),
  })

  // 하트비트
  useHeartbeat(stomp.sendHeartbeat, stomp.connected)

  // 채팅방 목록 로드
  useEffect(() => {
    roomApi.list().then((res) => {
      setRooms(res.data.data)
    }).catch(console.error)
  }, [])

  // 채팅방 선택 시 STOMP 구독
  useEffect(() => {
    if (activeRoomId === null) return
    stomp.subscribeRoom(activeRoomId)
    // 미읽음 초기화
    setRooms((prev) =>
      prev.map((r) => r.roomId === activeRoomId ? { ...r, unreadCount: 0 } : r)
    )
    return () => {
      // 방을 바꿀 때 이전 구독 유지 (백그라운드 알림을 위해 구독 해제하지 않음)
    }
  }, [activeRoomId, stomp])

  const handleRoomCreated = useCallback((roomId: number) => {
    roomApi.list().then((res) => {
      setRooms(res.data.data)
      setActiveRoomId(roomId)
    }).catch(console.error)
  }, [])

  const handleSendMessage = useCallback(
    (roomId: number, content: string) => {
      const req: ChatMessageRequest = { content, type: 'TEXT' }
      stomp.sendMessage(roomId, req)
    },
    [stomp]
  )

  const handleSendTyping = useCallback(
    (roomId: number, typing: boolean) => {
      stomp.sendTyping(roomId, typing)
    },
    [stomp]
  )

  const handleSendReadReceipt = useCallback(
    (roomId: number, messageId: string) => {
      stomp.sendReadReceipt(roomId, messageId)
    },
    [stomp]
  )

  const activeRoom = rooms.find((r) => r.roomId === activeRoomId) ?? null

  return (
    <div className="flex h-screen bg-gray-900 overflow-hidden">
      {/* ── 좌측 사이드바 ── */}
      <aside className="w-80 flex-shrink-0 flex flex-col bg-gray-800 border-r border-gray-700">
        {/* 사이드바 헤더 */}
        <div className="flex items-center justify-between px-4 py-4 border-b border-gray-700">
          <h1 className="text-lg font-bold text-white">채팅</h1>
          <div className="flex items-center gap-1">
            {/* 접속 상태 표시 */}
            <div
              className={`w-2 h-2 rounded-full ${stomp.connected ? 'bg-green-400' : 'bg-gray-500'}`}
              title={stomp.connected ? '연결됨' : '연결 중...'}
            />
            <button
              onClick={() => setShowCreateModal(true)}
              className="ml-2 p-2 text-gray-400 hover:text-white hover:bg-gray-700 rounded-lg transition-colors"
              title="새 채팅"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </button>
          </div>
        </div>

        {/* 채팅방 목록 */}
        <div className="flex-1 overflow-y-auto">
          {rooms.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-40 gap-2">
              <p className="text-gray-500 text-sm">채팅방이 없습니다.</p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="text-blue-400 text-sm hover:underline"
              >
                새 채팅 시작하기
              </button>
            </div>
          ) : (
            rooms.map((room) => (
              <RoomListItem
                key={room.roomId}
                room={room}
                active={room.roomId === activeRoomId}
                onClick={() => setActiveRoomId(room.roomId)}
                onlineStatuses={presenceMap}
              />
            ))
          )}
        </div>

        {/* 사용자 프로필 영역 */}
        <div className="flex items-center gap-3 px-4 py-3 border-t border-gray-700">
          <button
            onClick={() => setShowProfile(true)}
            className="flex items-center gap-3 flex-1 hover:bg-gray-700 rounded-lg p-1.5 transition-colors min-w-0"
          >
            <UserAvatar name={user.name} avatarUrl={user.avatarUrl} status="ONLINE" size="sm" />
            <div className="flex-1 min-w-0 text-left">
              <p className="text-sm font-medium text-white truncate">{user.name}</p>
              {user.statusMessage && (
                <p className="text-xs text-gray-400 truncate">{user.statusMessage}</p>
              )}
            </div>
          </button>
          <button
            onClick={onLogout}
            className="p-1.5 text-gray-400 hover:text-red-400 hover:bg-gray-700 rounded-lg transition-colors flex-shrink-0"
            title="로그아웃"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>
      </aside>

      {/* ── 우측 채팅 영역 ── */}
      <main className="flex-1 flex flex-col overflow-hidden">
        {activeRoom ? (
          <ChatRoomPage
            room={activeRoom}
            currentUser={user}
            onlineSatuses={presenceMap}
            onSendMessage={handleSendMessage}
            onSendTyping={handleSendTyping}
            onSendReadReceipt={handleSendReadReceipt}
            typingEvents={typingEvents}
            readReceiptEvents={readReceiptEvents}
            newMessages={messageBuffers[activeRoom.roomId] ?? []}
            onCallStart={setActiveCall}
          />
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center gap-4 text-gray-500">
            <svg className="w-16 h-16 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            <div className="text-center">
              <p className="text-lg font-medium text-gray-400">채팅방을 선택하세요</p>
              <p className="text-sm text-gray-600 mt-1">왼쪽에서 채팅방을 선택하거나 새 채팅을 시작하세요.</p>
            </div>
            <button
              onClick={() => setShowCreateModal(true)}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-sm rounded-lg transition-colors"
            >
              새 채팅 시작하기
            </button>
          </div>
        )}
      </main>

      {/* 모달들 */}
      <CreateRoomModal
        open={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleRoomCreated}
      />

      {showProfile && (
        <ProfilePage
          user={user}
          onUpdated={setUser}
          onClose={() => setShowProfile(false)}
        />
      )}

      {/* 수신 통화 알림 */}
      {incomingCall && (
        <IncomingCallNotificationBanner
          notification={incomingCall}
          onAccept={async () => {
            const res = await callApi.accept(incomingCall.callId)
            setActiveCall(res.data.data)
            setIncomingCall(null)
          }}
          onReject={async () => {
            await callApi.reject(incomingCall.callId)
            setIncomingCall(null)
          }}
        />
      )}

      {/* 활성 통화 */}
      {activeCall && (
        <VideoCallPage
          session={activeCall}
          currentUser={user}
          onEnd={() => setActiveCall(null)}
          onSdpOffer={stomp.sendSdpOffer}
          onSdpAnswer={stomp.sendSdpAnswer}
          onIceCandidate={stomp.sendIceCandidate}
        />
      )}
    </div>
  )
}

// ─── 수신 통화 배너 ────────────────────────────────────────────────────────────

type IncomingCallBannerProps = {
  notification: IncomingCallNotification
  onAccept: () => void
  onReject: () => void
}

function IncomingCallNotificationBanner({ notification, onAccept, onReject }: IncomingCallBannerProps) {
  return (
    <div className="fixed top-4 right-4 z-50 bg-gray-800 border border-gray-600 rounded-2xl shadow-2xl p-4 w-72">
      <div className="flex items-center gap-3 mb-3">
        <div className="w-10 h-10 bg-green-600 rounded-full flex items-center justify-center flex-shrink-0 animate-pulse">
          <svg className="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
            <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z" />
          </svg>
        </div>
        <div>
          <p className="text-white font-medium text-sm">{notification.initiatorName}</p>
          <p className="text-gray-400 text-xs">
            {notification.callType === 'VIDEO' ? '화상 통화' : '음성 통화'} 수신 중...
          </p>
        </div>
      </div>
      <div className="flex gap-2">
        <button
          onClick={onReject}
          className="flex-1 py-2 bg-red-600 hover:bg-red-500 text-white text-sm rounded-lg transition-colors"
        >
          거절
        </button>
        <button
          onClick={onAccept}
          className="flex-1 py-2 bg-green-600 hover:bg-green-500 text-white text-sm rounded-lg transition-colors"
        >
          수락
        </button>
      </div>
    </div>
  )
}
