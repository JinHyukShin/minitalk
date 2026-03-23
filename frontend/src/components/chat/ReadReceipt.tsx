type ReadReceiptProps = {
  readCount: number
  isMine: boolean
}

export function ReadReceipt({ readCount, isMine }: ReadReceiptProps) {
  if (!isMine || readCount === 0) return null

  return (
    <span className="inline-flex items-center gap-0.5 text-xs text-blue-400 ml-1">
      {/* Double check icon */}
      <svg className="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M0.41 13.41L6 19l1.41-1.41L0.41 11 0 11.41zm3.54-0.7L8 17.17 22.54 2.63 21.12 1.21 8 14.33l-4.95-4.95-1.42 1.41 2.32 2.32z" />
      </svg>
      {readCount > 1 && (
        <span className="text-gray-400">{readCount}</span>
      )}
    </span>
  )
}
