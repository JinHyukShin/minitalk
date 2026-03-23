import { useCallback, useEffect, useRef, useState } from 'react'
import { Client, type StompSubscription } from '@stomp/stompjs'
import type {
  Message,
  TypingEvent,
  ReadReceiptEvent,
  PresenceEvent,
  IncomingCallNotification,
  SdpAnswer,
  IceCandidateMessage,
  ChatMessageRequest,
} from '../types'

type StompCallbacks = {
  onMessage?: (msg: Message) => void
  onTyping?: (event: TypingEvent) => void
  onReadReceipt?: (event: ReadReceiptEvent) => void
  onPresence?: (event: PresenceEvent) => void
  onIncomingCall?: (notification: IncomingCallNotification) => void
  onSdpAnswer?: (answer: SdpAnswer) => void
  onIceCandidate?: (candidate: IceCandidateMessage) => void
}

type UseSTOMPReturn = {
  connected: boolean
  subscribeRoom: (roomId: number) => void
  unsubscribeRoom: (roomId: number) => void
  sendMessage: (roomId: number, req: ChatMessageRequest) => void
  sendTyping: (roomId: number, typing: boolean) => void
  sendReadReceipt: (roomId: number, messageId: string) => void
  sendHeartbeat: () => void
  sendSdpOffer: (payload: object) => void
  sendSdpAnswer: (payload: object) => void
  sendIceCandidate: (payload: object) => void
}

export function useSTOMP(callbacks: StompCallbacks): UseSTOMPReturn {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)
  const roomSubsRef = useRef<Map<number, StompSubscription[]>>(new Map())
  const callbacksRef = useRef(callbacks)

  useEffect(() => {
    callbacksRef.current = callbacks
  })

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (!token) return

    const client = new Client({
      brokerURL: `ws://${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true)

        // 개인 알림 구독
        client.subscribe('/user/queue/notifications', (frame) => {
          const notification = JSON.parse(frame.body) as IncomingCallNotification
          callbacksRef.current.onIncomingCall?.(notification)
        })

        // WebRTC 시그널링 구독
        client.subscribe('/user/queue/signal', (frame) => {
          const payload = JSON.parse(frame.body) as SdpAnswer | IceCandidateMessage
          if ('sdp' in payload) {
            callbacksRef.current.onSdpAnswer?.(payload as SdpAnswer)
          } else {
            callbacksRef.current.onIceCandidate?.(payload as IceCandidateMessage)
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
        setConnected(false)
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
    }
  }, [])

  const subscribeRoom = useCallback((roomId: number) => {
    const client = clientRef.current
    if (!client?.connected || roomSubsRef.current.has(roomId)) return

    const subs: StompSubscription[] = []

    subs.push(
      client.subscribe(`/topic/room/${roomId}`, (frame) => {
        const msg = JSON.parse(frame.body) as Message
        callbacksRef.current.onMessage?.(msg)
      })
    )

    subs.push(
      client.subscribe(`/topic/room/${roomId}/typing`, (frame) => {
        const event = JSON.parse(frame.body) as TypingEvent
        callbacksRef.current.onTyping?.(event)
      })
    )

    subs.push(
      client.subscribe(`/topic/room/${roomId}/read`, (frame) => {
        const event = JSON.parse(frame.body) as ReadReceiptEvent
        callbacksRef.current.onReadReceipt?.(event)
      })
    )

    subs.push(
      client.subscribe(`/topic/room/${roomId}/presence`, (frame) => {
        const event = JSON.parse(frame.body) as PresenceEvent
        callbacksRef.current.onPresence?.(event)
      })
    )

    roomSubsRef.current.set(roomId, subs)
  }, [])

  const unsubscribeRoom = useCallback((roomId: number) => {
    const subs = roomSubsRef.current.get(roomId)
    if (subs) {
      subs.forEach((s) => s.unsubscribe())
      roomSubsRef.current.delete(roomId)
    }
  }, [])

  const sendMessage = useCallback((roomId: number, req: ChatMessageRequest) => {
    clientRef.current?.publish({
      destination: `/app/chat/${roomId}`,
      body: JSON.stringify(req),
    })
  }, [])

  const sendTyping = useCallback((roomId: number, typing: boolean) => {
    clientRef.current?.publish({
      destination: `/app/chat/${roomId}/typing`,
      body: JSON.stringify({ typing }),
    })
  }, [])

  const sendReadReceipt = useCallback((roomId: number, messageId: string) => {
    clientRef.current?.publish({
      destination: `/app/chat/${roomId}/read`,
      body: JSON.stringify({ messageId }),
    })
  }, [])

  const sendHeartbeat = useCallback(() => {
    clientRef.current?.publish({
      destination: '/app/presence/heartbeat',
      body: '{}',
    })
  }, [])

  const sendSdpOffer = useCallback((payload: object) => {
    clientRef.current?.publish({
      destination: '/app/signal/sdp-offer',
      body: JSON.stringify(payload),
    })
  }, [])

  const sendSdpAnswer = useCallback((payload: object) => {
    clientRef.current?.publish({
      destination: '/app/signal/sdp-answer',
      body: JSON.stringify(payload),
    })
  }, [])

  const sendIceCandidate = useCallback((payload: object) => {
    clientRef.current?.publish({
      destination: '/app/signal/ice-candidate',
      body: JSON.stringify(payload),
    })
  }, [])

  return {
    connected,
    subscribeRoom,
    unsubscribeRoom,
    sendMessage,
    sendTyping,
    sendReadReceipt,
    sendHeartbeat,
    sendSdpOffer,
    sendSdpAnswer,
    sendIceCandidate,
  }
}
