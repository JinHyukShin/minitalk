import { useCallback, useRef, useState, type ChangeEvent, type KeyboardEvent } from 'react'

type MessageInputProps = {
  onSend: (content: string) => void
  onFileSelect: (file: File) => void
  onTypingStart: () => void
  onTypingStop: () => void
  disabled?: boolean
  placeholder?: string
}

const COMMON_EMOJIS = ['😊', '😂', '👍', '❤️', '🎉', '😢', '😮', '🙏', '🔥', '✨']

export function MessageInput({
  onSend,
  onFileSelect,
  onTypingStart,
  onTypingStop,
  disabled = false,
  placeholder = '메시지를 입력하세요...',
}: MessageInputProps) {
  const [content, setContent] = useState('')
  const [showEmoji, setShowEmoji] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const typingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const isTypingRef = useRef(false)

  const handleChange = useCallback(
    (e: ChangeEvent<HTMLTextAreaElement>) => {
      setContent(e.target.value)

      if (!isTypingRef.current) {
        isTypingRef.current = true
        onTypingStart()
      }

      if (typingTimerRef.current) clearTimeout(typingTimerRef.current)
      typingTimerRef.current = setTimeout(() => {
        isTypingRef.current = false
        onTypingStop()
      }, 1500)
    },
    [onTypingStart, onTypingStop]
  )

  const handleSend = useCallback(() => {
    const trimmed = content.trim()
    if (!trimmed) return
    onSend(trimmed)
    setContent('')
    if (typingTimerRef.current) clearTimeout(typingTimerRef.current)
    isTypingRef.current = false
    onTypingStop()
  }, [content, onSend, onTypingStop])

  const handleKeyDown = useCallback(
    (e: KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        handleSend()
      }
    },
    [handleSend]
  )

  const handleFileChange = useCallback(
    (e: ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0]
      if (file) {
        onFileSelect(file)
        e.target.value = ''
      }
    },
    [onFileSelect]
  )

  const insertEmoji = useCallback((emoji: string) => {
    setContent((prev) => prev + emoji)
    setShowEmoji(false)
  }, [])

  return (
    <div className="relative bg-gray-800 border-t border-gray-700">
      {/* Emoji picker */}
      {showEmoji && (
        <div className="absolute bottom-full left-4 mb-2 bg-gray-700 rounded-xl shadow-2xl p-3 flex flex-wrap gap-2 w-64">
          {COMMON_EMOJIS.map((emoji) => (
            <button
              key={emoji}
              onClick={() => insertEmoji(emoji)}
              className="text-2xl hover:bg-gray-600 rounded-lg p-1 transition-colors"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      <div className="flex items-end gap-2 px-4 py-3">
        {/* File attach */}
        <button
          onClick={() => fileInputRef.current?.click()}
          disabled={disabled}
          className="flex-shrink-0 p-2 text-gray-400 hover:text-white hover:bg-gray-700 rounded-lg transition-colors disabled:opacity-50"
          title="파일 첨부"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
            />
          </svg>
        </button>
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          onChange={handleFileChange}
          accept="image/*,application/pdf,.doc,.docx,.xls,.xlsx,.txt,.zip"
        />

        {/* Emoji button */}
        <button
          onClick={() => setShowEmoji((prev) => !prev)}
          disabled={disabled}
          className="flex-shrink-0 p-2 text-gray-400 hover:text-white hover:bg-gray-700 rounded-lg transition-colors disabled:opacity-50"
          title="이모지"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </button>

        {/* Text area */}
        <textarea
          value={content}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          placeholder={placeholder}
          rows={1}
          className="flex-1 bg-gray-700 text-white placeholder-gray-400 rounded-xl px-4 py-2.5 text-sm resize-none outline-none focus:ring-2 focus:ring-blue-500 max-h-32 overflow-y-auto disabled:opacity-50"
          style={{ minHeight: '42px' }}
        />

        {/* Send button */}
        <button
          onClick={handleSend}
          disabled={disabled || !content.trim()}
          className="flex-shrink-0 p-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          title="전송 (Enter)"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
            />
          </svg>
        </button>
      </div>
    </div>
  )
}
