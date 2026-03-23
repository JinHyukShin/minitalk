import { useCallback, useEffect, useRef, useState } from 'react'
import type { AxiosRequestConfig } from 'axios'
import { apiClient } from '../api/client'

type UseFetchState<T> = {
  data: T | null
  loading: boolean
  error: string | null
}

type UseFetchReturn<T> = UseFetchState<T> & {
  refetch: () => void
}

export function useFetch<T>(
  url: string,
  config?: AxiosRequestConfig,
  deps: unknown[] = []
): UseFetchReturn<T> {
  const [state, setState] = useState<UseFetchState<T>>({
    data: null,
    loading: true,
    error: null,
  })

  const configRef = useRef(config)
  useEffect(() => {
    configRef.current = config
  })

  const fetchData = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }))
    try {
      const res = await apiClient.get<{ data: T }>(url, configRef.current)
      setState({ data: res.data.data, loading: false, error: null })
    } catch (err) {
      const message = err instanceof Error ? err.message : '요청 중 오류가 발생했습니다.'
      setState({ data: null, loading: false, error: message })
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [url, ...deps])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  return { ...state, refetch: fetchData }
}
