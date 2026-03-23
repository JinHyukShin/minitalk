type TypingIndicatorProps = {
  typers: string[]
}

export function TypingIndicator({ typers }: TypingIndicatorProps) {
  if (typers.length === 0) return null

  const label =
    typers.length === 1
      ? `${typers[0]}이(가) 입력 중...`
      : typers.length === 2
      ? `${typers[0]}, ${typers[1]}이(가) 입력 중...`
      : `${typers[0]} 외 ${typers.length - 1}명이 입력 중...`

  return (
    <div className="flex items-center gap-2 px-4 py-1">
      {/* Animated dots */}
      <div className="flex gap-1 bg-gray-700 rounded-2xl px-3 py-2">
        {[0, 1, 2].map((i) => (
          <span
            key={i}
            className="w-2 h-2 rounded-full bg-gray-400"
            style={{ animation: `bounceDot 1.4s infinite ease-in-out ${i * 0.2}s` }}
          />
        ))}
      </div>
      <span className="text-xs text-gray-400">{label}</span>
    </div>
  )
}
