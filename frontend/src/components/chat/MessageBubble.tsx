import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import { ReadReceipt } from './ReadReceipt'
import { UserAvatar } from '../user/UserAvatar'
import { fileApi } from '../../api/client'
import type { Message } from '../../types'

type MessageBubbleProps = {
  message: Message
  currentUserId: number
  showAvatar?: boolean
}

function formatTime(iso: string): string {
  return format(new Date(iso), 'a h:mm', { locale: ko })
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export function MessageBubble({ message, currentUserId, showAvatar = true }: MessageBubbleProps) {
  const isMine = message.senderId === currentUserId

  if (message.type === 'SYSTEM') {
    return (
      <div className="flex justify-center my-2">
        <span className="px-4 py-1 text-xs text-gray-400 bg-gray-800 rounded-full">
          {message.content}
        </span>
      </div>
    )
  }

  return (
    <div className={`flex items-end gap-2 mb-1 ${isMine ? 'flex-row-reverse' : 'flex-row'}`}>
      {/* Avatar */}
      {!isMine && showAvatar && (
        <UserAvatar
          name={message.senderName}
          avatarUrl={message.senderAvatarUrl}
          size="sm"
        />
      )}
      {!isMine && !showAvatar && <div className="w-8 flex-shrink-0" />}

      <div className={`flex flex-col max-w-xs lg:max-w-md ${isMine ? 'items-end' : 'items-start'}`}>
        {/* Sender name */}
        {!isMine && showAvatar && (
          <span className="text-xs text-gray-400 mb-1 ml-1">{message.senderName}</span>
        )}

        {/* Bubble */}
        <div
          className={`relative rounded-2xl px-4 py-2 ${
            isMine
              ? 'bg-blue-600 text-white rounded-br-sm'
              : 'bg-gray-700 text-gray-100 rounded-bl-sm'
          }`}
        >
          {message.deleted ? (
            <span className="italic text-gray-400 text-sm">삭제된 메시지입니다.</span>
          ) : (
            <>
              {message.type === 'TEXT' && (
                <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
              )}

              {message.type === 'IMAGE' && message.attachment && (
                <div className="space-y-1">
                  <a
                    href={fileApi.downloadUrl(message.attachment.fileId)}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <img
                      src={message.attachment.thumbnailUrl ?? message.attachment.url}
                      alt={message.attachment.fileName}
                      className="max-w-[240px] max-h-[240px] rounded-lg object-cover cursor-pointer hover:opacity-90 transition-opacity"
                    />
                  </a>
                  {message.content && (
                    <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
                  )}
                </div>
              )}

              {message.type === 'FILE' && message.attachment && (
                <a
                  href={fileApi.downloadUrl(message.attachment.fileId)}
                  download={message.attachment.fileName}
                  className="flex items-center gap-3 hover:opacity-80 transition-opacity"
                >
                  <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center flex-shrink-0">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
                      />
                    </svg>
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium truncate">{message.attachment.fileName}</p>
                    <p className="text-xs opacity-70">{formatFileSize(message.attachment.fileSize)}</p>
                  </div>
                </a>
              )}
            </>
          )}
        </div>

        {/* Time + Read receipt */}
        <div className={`flex items-center gap-1 mt-0.5 ${isMine ? 'flex-row-reverse' : ''}`}>
          <span className="text-xs text-gray-500">{formatTime(message.createdAt)}</span>
          {isMine && (
            <ReadReceipt readCount={message.readBy.length} isMine={isMine} />
          )}
        </div>
      </div>
    </div>
  )
}
