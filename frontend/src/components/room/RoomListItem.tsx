import { format, isToday, isYesterday } from 'date-fns'
import { ko } from 'date-fns/locale'
import { Badge } from '../ui/Badge'
import { UserAvatar } from '../user/UserAvatar'
import type { ChatRoom, PresenceStatus } from '../../types'

type RoomListItemProps = {
  room: ChatRoom
  active: boolean
  onClick: () => void
  onlineStatuses?: Record<number, PresenceStatus>
}

function formatRoomTime(iso: string): string {
  const date = new Date(iso)
  if (isToday(date)) return format(date, 'a h:mm', { locale: ko })
  if (isYesterday(date)) return '어제'
  return format(date, 'M/d', { locale: ko })
}

export function RoomListItem({ room, active, onClick, onlineStatuses = {} }: RoomListItemProps) {
  const isGroup = room.type === 'GROUP'

  // 1:1 방의 경우 상대방 온라인 상태 표시
  const peerStatus: PresenceStatus | undefined =
    !isGroup && room.members.length > 0
      ? (onlineStatuses[room.members[0]?.userId ?? 0] ?? 'OFFLINE')
      : undefined

  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-700/60 transition-colors ${
        active ? 'bg-gray-700' : ''
      }`}
    >
      {/* Avatar */}
      <div className="flex-shrink-0">
        {isGroup ? (
          <div className="w-10 h-10 bg-indigo-600 rounded-full flex items-center justify-center">
            <svg className="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
            </svg>
          </div>
        ) : (
          <UserAvatar
            name={room.members[0]?.name ?? room.name}
            avatarUrl={room.members[0]?.avatarUrl ?? null}
            status={peerStatus}
            size="md"
          />
        )}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 min-w-0">
            <span className="text-sm font-medium text-white truncate">{room.name}</span>
            {isGroup && (
              <span className="text-xs text-gray-500 flex-shrink-0">{room.memberCount}</span>
            )}
          </div>
          <div className="flex flex-col items-end gap-1 flex-shrink-0 ml-2">
            {room.lastMessage && (
              <span className="text-xs text-gray-500">
                {formatRoomTime(room.lastMessage.sentAt)}
              </span>
            )}
            <Badge count={room.unreadCount} />
          </div>
        </div>

        {room.lastMessage && (
          <p className="text-xs text-gray-400 truncate mt-0.5">
            {room.type === 'GROUP' && room.lastMessage.type !== 'SYSTEM' && (
              <span className="text-gray-500">{room.lastMessage.senderName}: </span>
            )}
            {room.lastMessage.type === 'IMAGE'
              ? '사진'
              : room.lastMessage.type === 'FILE'
              ? '파일'
              : room.lastMessage.content}
          </p>
        )}
      </div>
    </button>
  )
}
