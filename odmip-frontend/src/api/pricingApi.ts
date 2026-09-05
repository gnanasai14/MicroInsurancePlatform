import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import type { RootState } from '../app/store'
import type {
  ApiResponse, Coupon, DashboardSummary, PremiumQuoteRequest, PremiumQuoteResponse,
  PricingRule, Quote, UsageLog, UsageResponse,
} from '../types'

const BASE = import.meta.env.VITE_PRICING_API_URL ?? 'http://localhost:8082'

export const pricingApi = createApi({
  reducerPath: 'pricingApi',
  baseQuery: fetchBaseQuery({
    baseUrl: BASE,
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as RootState).auth.token
      if (token) headers.set('Authorization', `Bearer ${token}`)
      return headers
    },
  }),
  tagTypes: ['Coupon', 'Usage', 'Dashboard', 'Rule', 'Quote'],
  endpoints: (builder) => ({
    quote: builder.mutation<PremiumQuoteResponse, PremiumQuoteRequest>({
      query: (body) => ({ url: '/api/pricing/quote', method: 'POST', body }),
      transformResponse: (r: ApiResponse<PremiumQuoteResponse>) => r.data,
    }),
    acceptQuote: builder.mutation<Quote, number>({
      query: (id) => ({ url: `/api/pricing/quote/${id}/accept`, method: 'POST' }),
      transformResponse: (r: ApiResponse<Quote>) => r.data,
      invalidatesTags: ['Dashboard', 'Quote'],
    }),
    cancelQuote: builder.mutation<Quote, number>({
      query: (id) => ({ url: `/api/pricing/quote/${id}/cancel`, method: 'POST' }),
      transformResponse: (r: ApiResponse<Quote>) => r.data,
    }),
    acceptedQuotes: builder.query<Quote[], void>({
      query: () => '/api/pricing/quote/accepted',
      transformResponse: (r: ApiResponse<Quote[]>) => r.data,
      providesTags: ['Quote'],
    }),
    analyticsSummary: builder.query<{ revenueByRiskCategory: Record<string, number>; couponRedemptions: Record<string, number> }, void>({
      query: () => '/api/pricing/analytics/summary',
      transformResponse: (r: ApiResponse<{ revenueByRiskCategory: Record<string, number>; couponRedemptions: Record<string, number> }>) => r.data,
    }),

    coupons: builder.query<Coupon[], void>({
      query: () => '/api/coupons',
      transformResponse: (r: ApiResponse<Coupon[]>) => r.data,
      providesTags: ['Coupon'],
    }),
    createCoupon: builder.mutation<Coupon, Partial<Coupon>>({
      query: (body) => ({ url: '/api/coupons', method: 'POST', body }),
      transformResponse: (r: ApiResponse<Coupon>) => r.data,
      invalidatesTags: ['Coupon'],
    }),
    validateCoupon: builder.query<{ valid: boolean; message: string }, { code: string; userId: number }>({
      query: ({ code, userId }) => `/api/coupons/${code}/validate?userId=${userId}`,
      transformResponse: (r: ApiResponse<{ valid: boolean; message: string }>) => r.data,
    }),

    recordUsage: builder.mutation<UsageResponse, { policyId: number; userId: number; usageType: string; quantity: number }>({
      query: (body) => ({ url: '/api/usage', method: 'POST', body }),
      transformResponse: (r: ApiResponse<UsageResponse>) => r.data,
      invalidatesTags: ['Usage', 'Dashboard'],
    }),
    usageForPolicy: builder.query<UsageLog[], number>({
      query: (policyId) => `/api/usage/policy/${policyId}`,
      transformResponse: (r: ApiResponse<UsageLog[]>) => r.data,
      providesTags: ['Usage'],
    }),
    usageTotal: builder.query<number, number>({
      query: (policyId) => `/api/usage/policy/${policyId}/total`,
      transformResponse: (r: ApiResponse<number>) => r.data,
      providesTags: ['Usage'],
    }),

    dashboard: builder.query<DashboardSummary, number>({
      query: (userId) => `/api/dashboard/${userId}`,
      transformResponse: (r: ApiResponse<DashboardSummary>) => r.data,
      providesTags: ['Dashboard'],
    }),

    pricingRules: builder.query<PricingRule[], void>({
      query: () => '/api/pricing/rules',
      transformResponse: (r: ApiResponse<PricingRule[]>) => r.data,
      providesTags: ['Rule'],
    }),
    createPricingRule: builder.mutation<PricingRule, Partial<PricingRule>>({
      query: (body) => ({ url: '/api/pricing/rules', method: 'POST', body }),
      transformResponse: (r: ApiResponse<PricingRule>) => r.data,
      invalidatesTags: ['Rule'],
    }),
    updatePricingRule: builder.mutation<PricingRule, { id: number; body: Partial<PricingRule> }>({
      query: ({ id, body }) => ({ url: `/api/pricing/rules/${id}`, method: 'PUT', body }),
      transformResponse: (r: ApiResponse<PricingRule>) => r.data,
      invalidatesTags: ['Rule'],
    }),
    deletePricingRule: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/pricing/rules/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Rule'],
    }),
  }),
})

export const {
  useQuoteMutation, useAcceptQuoteMutation, useCancelQuoteMutation, useAcceptedQuotesQuery,
  useAnalyticsSummaryQuery,
  useCouponsQuery, useCreateCouponMutation, useLazyValidateCouponQuery,
  useRecordUsageMutation, useUsageForPolicyQuery, useUsageTotalQuery,
  useDashboardQuery,
  usePricingRulesQuery, useCreatePricingRuleMutation, useUpdatePricingRuleMutation, useDeletePricingRuleMutation,
} = pricingApi
