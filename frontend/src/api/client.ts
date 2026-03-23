import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type {
  ApiError,
  ApiResponse,
  AuthResponse,
  LoginRequest,
  SignupRequest,
  UserProfile,
  UserSearchResult,
  ChatRoom,
  CreateRoomRequest,
  Message,
  PageResponse,
  FileAttachment,
  CallSession,
  CallStartRequest,
  PresenceEvent,
} from '../types'

const BASE_URL = '/api/v1'

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor: JWT 토큰 주입
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor: 토큰 갱신
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiError>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        const res = await axios.post<ApiResponse<AuthResponse>>(
          `${BASE_URL}/auth/refresh`,
          { refreshToken }
        )
        const { accessToken } = res.data.data
        localStorage.setItem('accessToken', accessToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return apiClient(originalRequest)
      } catch {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// ─── Auth API ─────────────────────────────────────────────────────────────────

export const authApi = {
  login: (req: LoginRequest) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/login', req),
  signup: (req: SignupRequest) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/signup', req),
  refresh: (refreshToken: string) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken }),
  logout: () =>
    apiClient.post<ApiResponse<null>>('/auth/logout'),
}

// ─── User API ─────────────────────────────────────────────────────────────────

export const userApi = {
  getMe: () =>
    apiClient.get<ApiResponse<UserProfile>>('/users/me'),
  updateProfile: (data: Partial<Pick<UserProfile, 'name' | 'statusMessage'>>) =>
    apiClient.put<ApiResponse<UserProfile>>('/users/me', data),
  search: (query: string) =>
    apiClient.get<ApiResponse<UserSearchResult[]>>(`/users/search?q=${encodeURIComponent(query)}`),
  getProfile: (userId: number) =>
    apiClient.get<ApiResponse<UserProfile>>(`/users/${userId}/profile`),
}

// ─── Room API ─────────────────────────────────────────────────────────────────

export const roomApi = {
  list: () =>
    apiClient.get<ApiResponse<ChatRoom[]>>('/rooms'),
  get: (roomId: number) =>
    apiClient.get<ApiResponse<ChatRoom>>(`/rooms/${roomId}`),
  create: (req: CreateRoomRequest) =>
    apiClient.post<ApiResponse<ChatRoom>>('/rooms', req),
  update: (roomId: number, data: { name?: string; avatarUrl?: string }) =>
    apiClient.put<ApiResponse<ChatRoom>>(`/rooms/${roomId}`, data),
  leave: (roomId: number) =>
    apiClient.post<ApiResponse<null>>(`/rooms/${roomId}/leave`),
  addMembers: (roomId: number, memberIds: number[]) =>
    apiClient.post<ApiResponse<ChatRoom>>(`/rooms/${roomId}/members`, { memberIds }),
  removeMember: (roomId: number, userId: number) =>
    apiClient.delete<ApiResponse<null>>(`/rooms/${roomId}/members/${userId}`),
}

// ─── Message API ──────────────────────────────────────────────────────────────

export const messageApi = {
  history: (roomId: number, cursor?: string, size = 30) =>
    apiClient.get<ApiResponse<PageResponse<Message>>>(
      `/rooms/${roomId}/messages`,
      { params: { cursor, size } }
    ),
  search: (roomId: number, query: string) =>
    apiClient.get<ApiResponse<Message[]>>(
      `/rooms/${roomId}/messages/search?q=${encodeURIComponent(query)}`
    ),
  edit: (messageId: string, content: string) =>
    apiClient.put<ApiResponse<Message>>(`/messages/${messageId}`, { content }),
  delete: (messageId: string) =>
    apiClient.delete<ApiResponse<null>>(`/messages/${messageId}`),
}

// ─── File API ─────────────────────────────────────────────────────────────────

export const fileApi = {
  upload: (roomId: number, file: File, onProgress?: (pct: number) => void) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient.post<ApiResponse<FileAttachment>>(
      `/rooms/${roomId}/files`,
      form,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (e) => {
          if (onProgress && e.total) {
            onProgress(Math.round((e.loaded / e.total) * 100))
          }
        },
      }
    )
  },
  downloadUrl: (fileId: string) => `${BASE_URL}/files/${fileId}/download`,
}

// ─── Call API ─────────────────────────────────────────────────────────────────

export const callApi = {
  start: (req: CallStartRequest) =>
    apiClient.post<ApiResponse<CallSession>>('/calls/start', req),
  accept: (callId: string) =>
    apiClient.post<ApiResponse<CallSession>>(`/calls/${callId}/accept`),
  reject: (callId: string) =>
    apiClient.post<ApiResponse<null>>(`/calls/${callId}/reject`),
  end: (callId: string) =>
    apiClient.post<ApiResponse<null>>(`/calls/${callId}/end`),
  join: (callId: string) =>
    apiClient.post<ApiResponse<CallSession>>(`/calls/${callId}/join`),
}

// ─── Presence API ─────────────────────────────────────────────────────────────

export const presenceApi = {
  getOnline: () =>
    apiClient.get<ApiResponse<PresenceEvent[]>>('/presence/online'),
  getStatus: (userId: number) =>
    apiClient.get<ApiResponse<PresenceEvent>>(`/presence/${userId}`),
}
