import { useEffect, useRef } from 'react'

type SSEOptions<T> = {
  url: string
  onMessage: (data: T) => void
  onError?: (error: Event) => void
  enabled?: boolean
}

export function useSSE<T>({ url, onMessage, onError, enabled = true }: SSEOptions<T>) {
  const esRef = useRef<EventSource | null>(null)
  const callbackRef = useRef({ onMessage, onError })

  useEffect(() => {
    callbackRef.current = { onMessage, onError }
  })

  useEffect(() => {
    if (!enabled) return

    const token = localStorage.getItem('accessToken')
    const fullUrl = token ? `${url}?token=${encodeURIComponent(token)}` : url

    const es = new EventSource(fullUrl)
    esRef.current = es

    es.onmessage = (event: MessageEvent<string>) => {
      try {
        const data = JSON.parse(event.data) as T
        callbackRef.current.onMessage(data)
      } catch {
        // ignore parse errors
      }
    }

    es.onerror = (event) => {
      callbackRef.current.onError?.(event)
    }

    return () => {
      es.close()
      esRef.current = null
    }
  }, [url, enabled])
}
