import { OnlineStatus } from './OnlineStatus'
import type { PresenceStatus } from '../../types'

type UserAvatarProps = {
  name: string
  avatarUrl: string | null
  status?: PresenceStatus
  size?: 'sm' | 'md' | 'lg' | 'xl'
}

const SIZE_CLASSES = {
  sm:  'w-8 h-8 text-xs',
  md:  'w-10 h-10 text-sm',
  lg:  'w-12 h-12 text-base',
  xl:  'w-16 h-16 text-xl',
}

const BADGE_OFFSET = {
  sm:  'bottom-0 right-0',
  md:  'bottom-0 right-0',
  lg:  '-bottom-0.5 -right-0.5',
  xl:  '-bottom-1 -right-1',
}

function getInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

function getColorFromName(name: string): string {
  const colors = [
    'bg-blue-500', 'bg-purple-500', 'bg-green-500',
    'bg-yellow-500', 'bg-red-500', 'bg-pink-500',
    'bg-indigo-500', 'bg-teal-500',
  ]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length] ?? 'bg-blue-500'
}

export function UserAvatar({ name, avatarUrl, status, size = 'md' }: UserAvatarProps) {
  return (
    <div className="relative inline-flex flex-shrink-0">
      {avatarUrl ? (
        <img
          src={avatarUrl}
          alt={name}
          className={`${SIZE_CLASSES[size]} rounded-full object-cover`}
        />
      ) : (
        <div
          className={`${SIZE_CLASSES[size]} ${getColorFromName(name)} rounded-full flex items-center justify-center font-semibold text-white`}
        >
          {getInitials(name)}
        </div>
      )}

      {status && (
        <span className={`absolute ${BADGE_OFFSET[size]}`}>
          <OnlineStatus status={status} size="sm" />
        </span>
      )}
    </div>
  )
}
