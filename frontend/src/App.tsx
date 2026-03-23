import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from './pages/LoginPage'
import { ChatListPage } from './pages/ChatListPage'
import { userApi, authApi } from './api/client'
import type { UserProfile } from './types'

function PrivateRoute({ children, user }: { children: React.ReactNode; user: UserProfile | null }) {
  const token = localStorage.getItem('accessToken')
  if (!token || !user) return <Navigate to="/login" replace />
  return <>{children}</>
}

function assertUser(user: UserProfile | null): UserProfile {
  if (!user) throw new Error('user is null')
  return user
}

export function App() {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [checking, setChecking] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      setChecking(false)
      return
    }
    userApi.getMe()
      .then((res) => setUser(res.data.data))
      .catch(() => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
      })
      .finally(() => setChecking(false))
  }, [])

  const handleLogout = useCallback(async () => {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setUser(null)
  }, [])

  if (checking) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/login"
          element={
            user ? (
              <Navigate to="/chat" replace />
            ) : (
              <LoginPage />
            )
          }
        />
        <Route
          path="/chat"
          element={
            <PrivateRoute user={user}>
              <ChatListPage
                currentUser={assertUser(user)}
                onLogout={handleLogout}
              />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<Navigate to={user ? '/chat' : '/login'} replace />} />
      </Routes>
    </BrowserRouter>
  )
}
