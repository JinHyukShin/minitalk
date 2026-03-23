import { useCallback, useState, type ChangeEvent, type FormEvent } from 'react'
import { userApi } from '../api/client'
import { UserAvatar } from '../components/user/UserAvatar'
import type { UserProfile } from '../types'

type ProfilePageProps = {
  user: UserProfile
  onUpdated: (updated: UserProfile) => void
  onClose: () => void
}

export function ProfilePage({ user, onUpdated, onClose }: ProfilePageProps) {
  const [name, setName] = useState(user.name)
  const [statusMessage, setStatusMessage] = useState(user.statusMessage ?? '')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = useCallback(
    async (e: FormEvent) => {
      e.preventDefault()
      if (!name.trim()) {
        setError('이름을 입력하세요.')
        return
      }
      setLoading(true)
      setError('')
      setSuccess(false)
      try {
        const res = await userApi.updateProfile({
          name: name.trim(),
          statusMessage: statusMessage.trim() || undefined,
        })
        onUpdated(res.data.data)
        setSuccess(true)
      } catch {
        setError('프로필 저장에 실패했습니다.')
      } finally {
        setLoading(false)
      }
    },
    [name, statusMessage, onUpdated]
  )

  const handleAvatarChange = useCallback((_e: ChangeEvent<HTMLInputElement>) => {
    // 실제 구현에서는 파일 업로드 후 avatarUrl 갱신
    alert('아바타 변경은 준비 중입니다.')
  }, [])

  return (
    <div className="fixed inset-0 z-40 bg-gray-900/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-gray-800 rounded-2xl w-full max-w-sm shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-700">
          <h2 className="text-lg font-semibold text-white">프로필 편집</h2>
          <button
            onClick={onClose}
            className="p-1 text-gray-400 hover:text-white rounded-lg hover:bg-gray-700 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Avatar */}
          <div className="flex flex-col items-center gap-3">
            <div className="relative">
              <UserAvatar name={user.name} avatarUrl={user.avatarUrl} size="xl" />
              <label className="absolute -bottom-1 -right-1 w-7 h-7 bg-blue-600 hover:bg-blue-500 rounded-full flex items-center justify-center cursor-pointer transition-colors shadow">
                <svg className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <input
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handleAvatarChange}
                />
              </label>
            </div>
            <p className="text-xs text-gray-400">{user.email}</p>
          </div>

          {/* Name */}
          <div>
            <label className="block text-xs text-gray-400 mb-1.5">이름</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="이름"
              required
              className="w-full bg-gray-700 text-white placeholder-gray-500 rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Status message */}
          <div>
            <label className="block text-xs text-gray-400 mb-1.5">상태 메시지</label>
            <input
              type="text"
              value={statusMessage}
              onChange={(e) => setStatusMessage(e.target.value)}
              placeholder="상태 메시지를 입력하세요"
              maxLength={100}
              className="w-full bg-gray-700 text-white placeholder-gray-500 rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="text-right text-xs text-gray-500 mt-1">{statusMessage.length}/100</p>
          </div>

          {error && <p className="text-sm text-red-400">{error}</p>}
          {success && <p className="text-sm text-green-400">프로필이 저장되었습니다.</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 bg-blue-600 hover:bg-blue-500 text-white text-sm font-semibold rounded-lg transition-colors disabled:opacity-50"
          >
            {loading ? '저장 중...' : '저장'}
          </button>
        </form>
      </div>
    </div>
  )
}
