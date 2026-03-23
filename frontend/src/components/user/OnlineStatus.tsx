import type { PresenceStatus } from '../../types'

type OnlineStatusProps = {
  status: PresenceStatus
  size?: 'sm' | 'md' | 'lg'
}

const SIZE_CLASSES = {
  sm: 'w-2 h-2',
  md: 'w-3 h-3',
  lg: 'w-4 h-4',
}

const STATUS_CLASSES: Record<PresenceStatus, string> = {
  ONLINE: 'bg-green-400',
  AWAY:   'bg-yellow-400',
  OFFLINE: 'bg-gray-500',
}

export function OnlineStatus({ status, size = 'md' }: OnlineStatusProps) {
  return (
    <span
      className={`inline-block rounded-full border-2 border-gray-800 ${SIZE_CLASSES[size]} ${STATUS_CLASSES[status]}`}
      aria-label={status.toLowerCase()}
    />
  )
}
