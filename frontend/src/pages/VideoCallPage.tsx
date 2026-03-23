import { useCallback, useEffect, useRef, useState } from 'react'
import { VideoCall } from '../components/call/VideoCall'
import { callApi } from '../api/client'
import type { CallSession, UserProfile } from '../types'

type VideoCallPageProps = {
  session: CallSession
  currentUser: UserProfile
  onEnd: () => void
  onSdpOffer: (payload: object) => void
  onSdpAnswer: (payload: object) => void  // SDP Answer 처리 (수신 측)
  onIceCandidate: (payload: object) => void
}

export function VideoCallPage({
  session,
  currentUser,
  onEnd,
  onSdpOffer,
  onSdpAnswer,
  onIceCandidate,
}: VideoCallPageProps) {
  // onSdpAnswer: 수신 측 SDP Answer 처리 시 사용 (현재는 발신 측 구현만 포함)
  void onSdpAnswer
  const [micEnabled, setMicEnabled] = useState(true)
  const [cameraEnabled, setCameraEnabled] = useState(session.callType === 'VIDEO')
  const [localStream, setLocalStream] = useState<MediaStream | null>(null)
  const [remoteStream] = useState<MediaStream | null>(null) // 실제 WebRTC 연동 시 업데이트
  const peerConnectionRef = useRef<RTCPeerConnection | null>(null)

  // 미디어 스트림 획득 (목업: 실제 WebRTC 스트림)
  useEffect(() => {
    const constraints: MediaStreamConstraints = {
      audio: true,
      video: session.callType === 'VIDEO',
    }

    navigator.mediaDevices
      .getUserMedia(constraints)
      .then((stream) => {
        setLocalStream(stream)

        // RTCPeerConnection 생성 (Kurento TURN/STUN 설정)
        const pc = new RTCPeerConnection({
          iceServers: [
            { urls: 'stun:stun.l.google.com:19302' },
            // TURN 서버는 서버로부터 동적으로 가져오는 방식으로 확장 가능
          ],
        })
        peerConnectionRef.current = pc

        stream.getTracks().forEach((track) => pc.addTrack(track, stream))

        pc.onicecandidate = (event) => {
          if (event.candidate) {
            onIceCandidate({
              callId: session.callId,
              candidate: event.candidate.candidate,
              sdpMid: event.candidate.sdpMid,
              sdpMLineIndex: event.candidate.sdpMLineIndex,
            })
          }
        }

        // SDP Offer 생성
        pc.createOffer()
          .then((offer) => pc.setLocalDescription(offer))
          .then(() => {
            if (pc.localDescription) {
              onSdpOffer({
                callId: session.callId,
                sdp: pc.localDescription.sdp,
              })
            }
          })
          .catch(console.error)
      })
      .catch((err) => {
        console.error('미디어 장치 접근 실패:', err)
      })

    return () => {
      localStream?.getTracks().forEach((t) => t.stop())
      peerConnectionRef.current?.close()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleToggleMic = useCallback(() => {
    setMicEnabled((prev) => {
      const next = !prev
      localStream?.getAudioTracks().forEach((t) => { t.enabled = next })
      return next
    })
  }, [localStream])

  const handleToggleCamera = useCallback(() => {
    setCameraEnabled((prev) => {
      const next = !prev
      localStream?.getVideoTracks().forEach((t) => { t.enabled = next })
      return next
    })
  }, [localStream])

  const handleEnd = useCallback(async () => {
    localStream?.getTracks().forEach((t) => t.stop())
    peerConnectionRef.current?.close()
    try {
      await callApi.end(session.callId)
    } catch {
      // 종료 실패해도 UI는 닫음
    }
    onEnd()
  }, [localStream, session.callId, onEnd])

  return (
    <div className="fixed inset-0 z-50 bg-gray-950">
      <VideoCall
        session={session}
        currentUserId={currentUser.id}
        localStream={localStream}
        remoteStream={remoteStream}
        micEnabled={micEnabled}
        cameraEnabled={cameraEnabled}
        onToggleMic={handleToggleMic}
        onToggleCamera={handleToggleCamera}
        onEnd={handleEnd}
      />
    </div>
  )
}
