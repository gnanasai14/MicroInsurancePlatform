import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { AuthResponse, Role } from '../../types'

interface AuthState {
  token: string | null
  username: string | null
  roles: Role[]
  userId: number | null
}

const stored = localStorage.getItem('odmip.auth')
const initialState: AuthState = stored
  ? JSON.parse(stored)
  : { token: null, username: null, roles: [], userId: null }

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    credentialsSet(state, action: PayloadAction<AuthResponse & { userId?: number }>) {
      state.token = action.payload.token
      state.username = action.payload.username
      state.roles = action.payload.roles
      if (action.payload.userId) state.userId = action.payload.userId
      localStorage.setItem('odmip.auth', JSON.stringify(state))
    },
    userIdSet(state, action: PayloadAction<number>) {
      state.userId = action.payload
      localStorage.setItem('odmip.auth', JSON.stringify(state))
    },
    loggedOut(state) {
      state.token = null
      state.username = null
      state.roles = []
      state.userId = null
      localStorage.removeItem('odmip.auth')
    },
  },
})

export const { credentialsSet, userIdSet, loggedOut } = authSlice.actions
export default authSlice.reducer
