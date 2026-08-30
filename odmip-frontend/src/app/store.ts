import { configureStore } from '@reduxjs/toolkit'
import authReducer from '../features/auth/authSlice'
import { userApi } from '../api/userApi'
import { pricingApi } from '../api/pricingApi'
import { claimsApi } from '../api/claimsApi'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [userApi.reducerPath]: userApi.reducer,
    [pricingApi.reducerPath]: pricingApi.reducer,
    [claimsApi.reducerPath]: claimsApi.reducer,
  },
  middleware: (getDefault) =>
    getDefault().concat(userApi.middleware, pricingApi.middleware, claimsApi.middleware),
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
