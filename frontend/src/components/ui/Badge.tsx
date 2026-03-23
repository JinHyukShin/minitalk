type BadgeProps = {
  count: number
  max?: number
}

export function Badge({ count, max = 99 }: BadgeProps) {
  if (count <= 0) return null
  const label = count > max ? `${max}+` : String(count)

  return (
    <span className="inline-flex items-center justify-center min-w-[1.25rem] h-5 px-1 rounded-full bg-red-500 text-white text-xs font-bold leading-none">
      {label}
    </span>
  )
}
