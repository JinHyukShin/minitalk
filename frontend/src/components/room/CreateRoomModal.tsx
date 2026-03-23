import { useCallback, useState } from 'react'
import { Modal } from '../ui/Modal'
import { UserAvatar } from '../user/UserAvatar'
import { userApi, roomApi } from '../../api/client'
import type { ChatRoomType, UserSearchResult } from '../../types'

type CreateRoomModalProps = {
  open: boolean
  onClose: () => void
  onCreated: (roomId: number) => void
}

export function CreateRoomModal({ open, onClose, onCreated }: CreateRoomModalProps) {
  const [roomType, setRoomType] = useState<ChatRoomType>('DIRECT')
  const [groupName, setGroupName] = useState('')
  const [query, setQuery] = useState('')
  const [searchResults, setSearchResults] = useState<UserSearchResult[]>([])
  const [selectedUsers, setSelectedUsers] = useState<UserSearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')

  const handleSearch = useCallback(async () => {
    if (!query.trim()) return
    setSearching(true)
    try {
      const res = await userApi.search(query.trim())
      setSearchResults(res.data.data)
    } catch {
      setError('사용자 검색에 실패했습니다.')
    } finally {
      setSearching(false)
    }
  }, [query])

  const handleSelectUser = useCallback((user: UserSearchResult) => {
    setSelectedUsers((prev) => {
      if (prev.find((u) => u.id === user.id)) return prev
      if (roomType === 'DIRECT') return [user]
      return [...prev, user]
    })
  }, [roomType])

  const handleRemoveUser = useCallback((userId: number) => {
    setSelectedUsers((prev) => prev.filter((u) => u.id !== userId))
  }, [])

  const handleCreate = useCallback(async () => {
    if (selectedUsers.length === 0) {
      setError('대화 상대를 선택하세요.')
      return
    }
    if (roomType === 'GROUP' && !groupName.trim()) {
      setError('그룹 이름을 입력하세요.')
      return
    }

    setCreating(true)
    setError('')
    try {
      const res = await roomApi.create({
        type: roomType,
        name: roomType === 'GROUP' ? groupName.trim() : undefined,
        memberIds: selectedUsers.map((u) => u.id),
      })
      onCreated(res.data.data.roomId)
      onClose()
      // reset
      setQuery('')
      setSearchResults([])
      setSelectedUsers([])
      setGroupName('')
      setRoomType('DIRECT')
    } catch {
      setError('채팅방 생성에 실패했습니다.')
    } finally {
      setCreating(false)
    }
  }, [selectedUsers, roomType, groupName, onCreated, onClose])

  const handleClose = useCallback(() => {
    setQuery('')
    setSearchResults([])
    setSelectedUsers([])
    setGroupName('')
    setRoomType('DIRECT')
    setError('')
    onClose()
  }, [onClose])

  return (
    <Modal open={open} onClose={handleClose} title="새 채팅">
      <div className="space-y-4">
        {/* 채팅 유형 선택 */}
        <div className="flex gap-2">
          {(['DIRECT', 'GROUP'] as ChatRoomType[]).map((type) => (
            <button
              key={type}
              onClick={() => { setRoomType(type); setSelectedUsers([]) }}
              className={`flex-1 py-2 text-sm rounded-lg font-medium transition-colors ${
                roomType === type
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
            >
              {type === 'DIRECT' ? '1:1 채팅' : '그룹 채팅'}
            </button>
          ))}
        </div>

        {/* 그룹 이름 */}
        {roomType === 'GROUP' && (
          <input
            type="text"
            value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            placeholder="그룹 이름"
            className="w-full bg-gray-700 text-white placeholder-gray-400 rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
        )}

        {/* 사용자 검색 */}
        <div className="flex gap-2">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="이름 또는 이메일로 검색"
            className="flex-1 bg-gray-700 text-white placeholder-gray-400 rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSearch}
            disabled={searching || !query.trim()}
            className="px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm rounded-lg transition-colors disabled:opacity-50"
          >
            검색
          </button>
        </div>

        {/* 검색 결과 */}
        {searchResults.length > 0 && (
          <div className="max-h-40 overflow-y-auto space-y-1 rounded-lg border border-gray-700">
            {searchResults.map((user) => {
              const isSelected = selectedUsers.some((u) => u.id === user.id)
              return (
                <button
                  key={user.id}
                  onClick={() => handleSelectUser(user)}
                  className={`w-full flex items-center gap-3 px-3 py-2 hover:bg-gray-700 transition-colors ${
                    isSelected ? 'bg-blue-900/40' : ''
                  }`}
                >
                  <UserAvatar name={user.name} avatarUrl={user.avatarUrl} size="sm" />
                  <div className="flex-1 text-left min-w-0">
                    <p className="text-sm text-white truncate">{user.name}</p>
                    <p className="text-xs text-gray-400 truncate">{user.email}</p>
                  </div>
                  {isSelected && (
                    <svg className="w-4 h-4 text-blue-400 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z" />
                    </svg>
                  )}
                </button>
              )
            })}
          </div>
        )}

        {/* 선택된 사용자 */}
        {selectedUsers.length > 0 && (
          <div>
            <p className="text-xs text-gray-400 mb-2">선택된 사용자</p>
            <div className="flex flex-wrap gap-2">
              {selectedUsers.map((user) => (
                <div
                  key={user.id}
                  className="flex items-center gap-1.5 bg-blue-900/50 text-blue-300 rounded-full pl-2 pr-1 py-1 text-xs"
                >
                  <span>{user.name}</span>
                  <button
                    onClick={() => handleRemoveUser(user.id)}
                    className="w-4 h-4 rounded-full hover:bg-blue-700 flex items-center justify-center"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 에러 */}
        {error && <p className="text-sm text-red-400">{error}</p>}

        {/* 생성 버튼 */}
        <button
          onClick={handleCreate}
          disabled={creating || selectedUsers.length === 0}
          className="w-full py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {creating ? '생성 중...' : '채팅 시작'}
        </button>
      </div>
    </Modal>
  )
}
