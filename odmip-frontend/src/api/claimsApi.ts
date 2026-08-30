import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import type { RootState } from '../app/store'
import type { ApiResponse, Claim, FraudFlag, FraudRule, RiskScore } from '../types'

const BASE = import.meta.env.VITE_CLAIMS_API_URL ?? 'http://localhost:8083'

export const claimsApi = createApi({
  reducerPath: 'claimsApi',
  baseQuery: fetchBaseQuery({
    baseUrl: BASE,
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as RootState).auth.token
      if (token) headers.set('Authorization', `Bearer ${token}`)
      return headers
    },
  }),
  tagTypes: ['Claim', 'FraudRule'],
  endpoints: (builder) => ({
    submitClaim: builder.mutation<Claim, { policyId: number; userId: number; claimedAmount: number; description?: string }>({
      query: (body) => ({ url: '/api/claims', method: 'POST', body }),
      transformResponse: (r: ApiResponse<Claim>) => r.data,
      invalidatesTags: ['Claim'],
    }),
    updateClaimStatus: builder.mutation<Claim, { id: number; newStatus: string; note?: string }>({
      query: ({ id, ...body }) => ({ url: `/api/claims/${id}/status`, method: 'PATCH', body }),
      transformResponse: (r: ApiResponse<Claim>) => r.data,
      invalidatesTags: ['Claim'],
    }),
    claimById: builder.query<Claim, number>({
      query: (id) => `/api/claims/${id}`,
      transformResponse: (r: ApiResponse<Claim>) => r.data,
      providesTags: ['Claim'],
    }),
    claimsByUser: builder.query<Claim[], number>({
      query: (userId) => `/api/claims/user/${userId}`,
      transformResponse: (r: ApiResponse<Claim[]>) => r.data,
      providesTags: ['Claim'],
    }),
    claimsByPolicy: builder.query<Claim[], number>({
      query: (policyId) => `/api/claims/policy/${policyId}`,
      transformResponse: (r: ApiResponse<Claim[]>) => r.data,
      providesTags: ['Claim'],
    }),
    claimNotifications: builder.query<Record<string, unknown>[], number>({
      query: (id) => `/api/claims/${id}/notifications`,
      transformResponse: (r: ApiResponse<Record<string, unknown>[]>) => r.data,
    }),

    riskScore: builder.query<RiskScore, number>({
      query: (claimId) => `/api/risk/claims/${claimId}/score`,
      transformResponse: (r: ApiResponse<RiskScore>) => r.data,
    }),
    fraudFlags: builder.query<FraudFlag[], number>({
      query: (claimId) => `/api/risk/claims/${claimId}/flags`,
      transformResponse: (r: ApiResponse<FraudFlag[]>) => r.data,
    }),
    fraudRules: builder.query<FraudRule[], void>({
      query: () => '/api/risk/rules',
      transformResponse: (r: ApiResponse<FraudRule[]>) => r.data,
      providesTags: ['FraudRule'],
    }),
    createFraudRule: builder.mutation<FraudRule, Partial<FraudRule>>({
      query: (body) => ({ url: '/api/risk/rules', method: 'POST', body }),
      transformResponse: (r: ApiResponse<FraudRule>) => r.data,
      invalidatesTags: ['FraudRule'],
    }),
  }),
})

export const {
  useSubmitClaimMutation, useUpdateClaimStatusMutation,
  useClaimByIdQuery, useClaimsByUserQuery, useClaimsByPolicyQuery, useClaimNotificationsQuery,
  useRiskScoreQuery, useFraudFlagsQuery,
  useFraudRulesQuery, useCreateFraudRuleMutation,
} = claimsApi
