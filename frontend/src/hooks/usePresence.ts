import { useCallback, useEffect, useRef, useState } from 'react'
import type { PresenceStatus } from '../types'

type PresenceMap = Record<number, PresenceStatus>

export function usePresence(initialMap: PresenceMap = {}) {
  const [presenceMap, setPresenceMap] = useState<PresenceMap>(initialMap)

  const updatePresence = useCallback((userId: number, status: PresenceStatus) => {
    setPresenceMap((prev) => ({ ...prev, [userId]: status }))
  }, [])

  const isOnline = useCallback(
    (userId: number) => presenceMap[userId] === 'ONLINE',
    [presenceMap]
  )

  const getStatus = useCallback(
    (userId: number): PresenceStatus => presenceMap[userId] ?? 'OFFLINE',
    [presenceMap]
  )

  return { presenceMap, updatePresence, isOnline, getStatus }
}

// ─── 하트비트 훅 ──────────────────────────────────────────────────────────────

const HEARTBEAT_INTERVAL_MS = 30_000

export function useHeartbeat(sendHeartbeat: () => void, enabled: boolean) {
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    if (!enabled) return

    sendHeartbeat()
    intervalRef.current = setInterval(sendHeartbeat, HEARTBEAT_INTERVAL_MS)

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current)
    }
  }, [sendHeartbeat, enabled])
}
