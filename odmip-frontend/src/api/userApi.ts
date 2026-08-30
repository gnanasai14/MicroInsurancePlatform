import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import type { RootState } from '../app/store'
import type {
  ApiResponse, AuthResponse, Policy, PolicyPremiumHistoryEntry, PolicyTemplate, UserProfile,
} from '../types'

const BASE = import.meta.env.VITE_USER_API_URL ?? 'http://localhost:8081'

export const userApi = createApi({
  reducerPath: 'userApi',
  baseQuery: fetchBaseQuery({
    baseUrl: BASE,
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as RootState).auth.token
      if (token) headers.set('Authorization', `Bearer ${token}`)
      return headers
    },
  }),
  tagTypes: ['Policy', 'Template', 'Users', 'PricingRules', 'PremiumHistory'],
  endpoints: (builder) => ({
    register: builder.mutation<
      { username: string; email: string; message: string },
      { firstName: string; lastName: string; username: string; email: string; sms: string; password: string }
    >({
      query: (body) => ({ url: '/api/auth/register', method: 'POST', body }),
      transformResponse: (r: ApiResponse<{ username: string; email: string; message: string }>) => r.data,
    }),
    verifyOtp: builder.mutation<AuthResponse, { username: string; otp: string }>({
      query: (body) => ({ url: '/api/auth/verify-otp', method: 'POST', body }),
      transformResponse: (r: ApiResponse<AuthResponse>) => r.data,
    }),
    resendOtp: builder.mutation<{ username: string; email: string; message: string }, { username: string }>({
      query: (body) => ({ url: '/api/auth/resend-otp', method: 'POST', body }),
      transformResponse: (r: ApiResponse<{ username: string; email: string; message: string }>) => r.data,
    }),
    login: builder.mutation<AuthResponse, { username: string; password: string }>({
      query: (body) => ({ url: '/api/auth/login', method: 'POST', body }),
      transformResponse: (r: ApiResponse<AuthResponse>) => r.data,
    }),
    me: builder.query<{ id: number; username: string; email: string; roles: string[] }, void>({
      query: () => '/api/auth/me',
      transformResponse: (r: ApiResponse<{ id: number; username: string; email: string; roles: string[] }>) => r.data,
    }),

    templates: builder.query<PolicyTemplate[], void>({
      query: () => '/api/templates',
      transformResponse: (r: ApiResponse<PolicyTemplate[]>) => r.data,
      providesTags: ['Template'],
    }),
    createTemplate: builder.mutation<PolicyTemplate, Partial<PolicyTemplate>>({
      query: (body) => ({ url: '/api/templates', method: 'POST', body }),
      transformResponse: (r: ApiResponse<PolicyTemplate>) => r.data,
      invalidatesTags: ['Template'],
    }),

    policiesByUser: builder.query<Policy[], number>({
      query: (userId) => `/api/policies/user/${userId}`,
      transformResponse: (r: ApiResponse<Policy[]>) => r.data,
      providesTags: ['Policy'],
    }),
    policyById: builder.query<Policy, number>({
      query: (id) => `/api/policies/${id}`,
      transformResponse: (r: ApiResponse<Policy>) => r.data,
      providesTags: ['Policy'],
    }),
    premiumHistory: builder.query<PolicyPremiumHistoryEntry[], number>({
      query: (id) => `/api/policies/${id}/premium-history`,
      transformResponse: (r: ApiResponse<PolicyPremiumHistoryEntry[]>) => r.data,
      providesTags: ['PremiumHistory'],
    }),
    createPolicy: builder.mutation<Policy, { userId: number; templateCode: string; durationHoursOverride?: number }>({
      query: (body) => ({ url: '/api/policies', method: 'POST', body }),
      transformResponse: (r: ApiResponse<Policy>) => r.data,
      invalidatesTags: ['Policy'],
    }),
    activatePolicy: builder.mutation<Policy, number>({
      query: (id) => ({ url: `/api/policies/${id}/activate`, method: 'POST' }),
      transformResponse: (r: ApiResponse<Policy>) => r.data,
      invalidatesTags: ['Policy'],
    }),
    cancelPolicy: builder.mutation<Policy, number>({
      query: (id) => ({ url: `/api/policies/${id}/cancel`, method: 'POST' }),
      transformResponse: (r: ApiResponse<Policy>) => r.data,
      invalidatesTags: ['Policy'],
    }),

    adminUsers: builder.query<UserProfile[], void>({
      query: () => '/api/admin/users',
      transformResponse: (r: ApiResponse<UserProfile[]>) => r.data,
      providesTags: ['Users'],
    }),
    adminPolicies: builder.query<Policy[], void>({
      query: () => '/api/admin/policies',
      transformResponse: (r: ApiResponse<Policy[]>) => r.data,
      providesTags: ['Policy'],
    }),
    disableUser: builder.mutation<UserProfile, number>({
      query: (id) => ({ url: `/api/admin/users/${id}/disable`, method: 'POST' }),
      invalidatesTags: ['Users'],
    }),
    adminPricingRules: builder.query<Record<string, unknown>[], void>({
      query: () => '/api/admin/pricing-rules',
      transformResponse: (r: ApiResponse<Record<string, unknown>[]>) => r.data,
      providesTags: ['PricingRules'],
    }),
    adminCreatePricingRule: builder.mutation<Record<string, unknown>, Record<string, unknown>>({
      query: (body) => ({ url: '/api/admin/pricing-rules', method: 'POST', body }),
      invalidatesTags: ['PricingRules'],
    }),
    adminDeletePricingRule: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/admin/pricing-rules/${id}`, method: 'DELETE' }),
      invalidatesTags: ['PricingRules'],
    }),
  }),
})

export const {
  useRegisterMutation, useVerifyOtpMutation, useResendOtpMutation, useLoginMutation, useMeQuery,
  useTemplatesQuery, useCreateTemplateMutation,
  usePoliciesByUserQuery, usePolicyByIdQuery, usePremiumHistoryQuery,
  useCreatePolicyMutation, useActivatePolicyMutation, useCancelPolicyMutation,
  useAdminUsersQuery, useAdminPoliciesQuery, useDisableUserMutation,
  useAdminPricingRulesQuery, useAdminCreatePricingRuleMutation, useAdminDeletePricingRuleMutation,
} = userApi
