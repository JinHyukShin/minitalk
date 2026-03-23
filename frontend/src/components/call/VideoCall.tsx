import { useEffect, useRef } from 'react'
import { CallControls } from './CallControls'
import { UserAvatar } from '../user/UserAvatar'
import type { CallSession } from '../../types'

type VideoCallProps = {
  session: CallSession
  currentUserId: number
  localStream: MediaStream | null
  remoteStream: MediaStream | null
  micEnabled: boolean
  cameraEnabled: boolean
  onToggleMic: () => void
  onToggleCamera: () => void
  onEnd: () => void
}

export function VideoCall({
  session,
  currentUserId,
  localStream,
  remoteStream,
  micEnabled,
  cameraEnabled,
  onToggleMic,
  onToggleCamera,
  onEnd,
}: VideoCallProps) {
  const localVideoRef = useRef<HTMLVideoElement>(null)
  const remoteVideoRef = useRef<HTMLVideoElement>(null)

  useEffect(() => {
    if (localVideoRef.current && localStream) {
      localVideoRef.current.srcObject = localStream
    }
  }, [localStream])

  useEffect(() => {
    if (remoteVideoRef.current && remoteStream) {
      remoteVideoRef.current.srcObject = remoteStream
    }
  }, [remoteStream])

  const remoteParticipants = session.participants.filter((p) => p.userId !== currentUserId)
  const firstRemote = remoteParticipants[0]

  return (
    <div className="relative w-full h-full bg-gray-900 flex flex-col overflow-hidden">
      {/* Remote video (main area) */}
      <div className="flex-1 relative flex items-center justify-center bg-gray-900">
        {remoteStream ? (
          <video
            ref={remoteVideoRef}
            autoPlay
            playsInline
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="flex flex-col items-center gap-4">
            {firstRemote && (
              <UserAvatar
                name={firstRemote.userName}
                avatarUrl={firstRemote.avatarUrl}
                size="xl"
              />
            )}
            <p className="text-gray-300 text-lg">
              {session.status === 'RINGING' ? '연결 중...' : '상대방 영상 없음'}
            </p>
          </div>
        )}

        {/* Participant count badge (group call) */}
        {session.participants.length > 2 && (
          <div className="absolute top-4 left-4 bg-black/50 backdrop-blur-sm rounded-full px-3 py-1">
            <span className="text-white text-sm">
              {session.participants.filter((p) => p.joined).length}명 참가 중
            </span>
          </div>
        )}
      </div>

      {/* Local video (picture-in-picture) */}
      <div className="absolute bottom-24 right-4 w-28 h-40 rounded-xl overflow-hidden border-2 border-gray-700 shadow-2xl bg-gray-800">
        {localStream && cameraEnabled ? (
          <video
            ref={localVideoRef}
            autoPlay
            playsInline
            muted
            className="w-full h-full object-cover mirror"
            style={{ transform: 'scaleX(-1)' }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gray-800">
            <svg className="w-8 h-8 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.069A1 1 0 0121 8.87v6.26a1 1 0 01-1.447.894L15 14M3 3l18 18" />
            </svg>
          </div>
        )}
      </div>

      {/* Controls */}
      <div className="absolute bottom-4 left-0 right-0 flex justify-center">
        <CallControls
          micEnabled={micEnabled}
          cameraEnabled={cameraEnabled}
          onToggleMic={onToggleMic}
          onToggleCamera={onToggleCamera}
          onEnd={onEnd}
        />
      </div>
    </div>
  )
}
