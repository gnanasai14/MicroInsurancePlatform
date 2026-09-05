// Mirrors the actual backend DTOs/entities (see repo's FIXES.md for API history).

export type Role = 'ROLE_USER' | 'ROLE_ADMIN' | 'ROLE_UNDERWRITER'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp?: string
}

export interface AuthResponse {
  token: string
  username: string
  roles: Role[]
}

export interface UserProfile {
  id: number
  username: string
  email: string
  status: string
  roles: string[]
  emailAlertsEnabled: boolean
  smsAlertsEnabled: boolean
}

export type PolicyStatus = 'DRAFT' | 'ACTIVE' | 'EXPIRED' | 'CANCELLED'

export interface PolicyTemplate {
  id: number
  code: string
  name: string
  description?: string
  baseCoverageAmount: number
  basePremium: number
  defaultDurationHours: number
  riskCategory: string
  usageCap?: number | null
  active: boolean
}

export interface Policy {
  id: number
  policyNumber: string
  userId: number
  template: PolicyTemplate
  status: PolicyStatus
  coverageAmount: number
  premiumAmount: number
  startDate: string
  endDate: string
  createdAt: string
  usageCap?: number | null
}

export interface PolicyPremiumHistoryEntry {
  id: number
  policyId: number
  premiumAmount: number
  changedAt: string
}

export interface PremiumQuoteRequest {
  basePremium: number
  riskCategory: string
  location?: string
  usageLevel?: string
  durationHours: number
  couponCode?: string
  policyId?: number
  userId?: number
}

export interface PremiumQuoteResponse {
  quoteId: number
  status: 'PENDING' | 'ACCEPTED' | 'CANCELLED'
  basePremium: number
  appliedRules: string[]
  multiplierApplied: number
  premiumBeforeDiscount: number
  discountApplied: number
  finalPremium: number
}

export interface Quote {
  id: number
  policyId: number | null
  userId: number
  riskCategory: string
  basePremium: number
  finalPremium: number
  couponCode: string | null
  discountAmount: number
  createdAt: string
  status: 'PENDING' | 'ACCEPTED' | 'CANCELLED'
  decidedAt: string | null
}

export interface Coupon {
  id: number
  code: string
  discountPercent: number
  validFrom: string
  validUntil: string
  maxRedemptions: number
  maxRedemptionsPerUser?: number
  timesRedeemed: number
  active: boolean
}

export interface UsageLog {
  id: number
  policyId: number
  userId: number
  usageType: string
  quantity: number
  recordedAt: string
}

export interface UsageResponse {
  usageLog: UsageLog
  totalUsage: number
  usageCap: number | null
  percentage: number
  thresholdCrossed: 'WARNING_80_PERCENT' | 'CAP_REACHED' | null
}

export interface DashboardSummary {
  userId: number
  activePolicyCount: number
  totalPolicyCount: number
  totalPremiumPaid: number
  policyUsage: { policyId: number; policyNumber: string; status: string; totalUsage: number }[]
}

export interface PricingRule {
  id: number
  type: 'RISK' | 'LOCATION' | 'USAGE' | 'SURGE'
  matchValue: string
  multiplier: number
  active: boolean
}

export type ClaimStatus = 'SUBMITTED' | 'VALIDATED' | 'UNDER_REVIEW' | 'ON_HOLD' | 'APPROVED' | 'REJECTED'

export interface Claim {
  id: number
  claimNumber: string
  policyId: number
  userId: number
  claimedAmount: number
  description?: string
  status: ClaimStatus
  policyValidated: boolean
  submittedAt: string
}

export interface RiskScore {
  id: number
  claimId: number
  score: number
}

export interface FraudFlag {
  id: number
  claimId: number
  ruleTriggered: string
  reason: string
}

export interface FraudRule {
  id: number
  ruleCode: string
  description?: string
  conditionType: string
  thresholdAmount?: number
  thresholdCount?: number
  riskScoreWeight: number
  active: boolean
}
