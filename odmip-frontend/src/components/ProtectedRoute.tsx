import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAppSelector } from '../app/hooks'

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const token = useAppSelector((s) => s.auth.token)
  if (!token) return <Navigate to="/login" replace />
  return <>{children}</>
}

export function AdminRoute({ children }: { children: ReactNode }) {
  const { token, roles } = useAppSelector((s) => s.auth)
  if (!token) return <Navigate to="/login" replace />
  if (!roles.includes('ROLE_ADMIN')) return <Navigate to="/" replace />
  return <>{children}</>
}
