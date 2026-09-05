import { useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useAppSelector } from '../app/hooks'
import { useEnsureUserId } from '../app/useEnsureUserId'
import {
  useAcceptQuoteMutation, useCancelQuoteMutation, useCouponsQuery, useCreateCouponMutation,
  useLazyValidateCouponQuery, useQuoteMutation, useRecordUsageMutation,
} from '../api/pricingApi'
import { usePoliciesByUserQuery } from '../api/userApi'
import { Button, Card, EmptyState, Field, inputClass, Money } from '../components/ui'
import type { UsageResponse } from '../types'

type QuoteStep = 'form' | 'review' | 'payment' | 'confirmed'

function QuoteTool({ prefill }: { prefill?: { policyId: number; policyNumber: string; basePremium: number; riskCategory: string; durationHours: number } }) {
  const userId = useEnsureUserId()
  const [form, setForm] = useState({
    basePremium: prefill?.basePremium ?? 25,
    riskCategory: prefill?.riskCategory ?? 'MEDIUM',
    location: 'URBAN', usageLevel: 'MODERATE',
    durationHours: prefill?.durationHours ?? 24, couponCode: '',
  })
  const [step, setStep] = useState<QuoteStep>('form')
  const [quote, { data, isLoading, error }] = useQuoteMutation()
  const [acceptQuote, { isLoading: paying }] = useAcceptQuoteMutation()
  const [cancelQuote] = useCancelQuoteMutation()

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      await quote({
        ...form, userId: userId ?? undefined, couponCode: form.couponCode || undefined,
        policyId: prefill?.policyId,
      }).unwrap()
      setStep('review')
    } catch (err) {
      console.error('Quote request failed:', err)
    }
  }

  async function onNotNow() {
    if (data) await cancelQuote(data.quoteId).catch(() => {})
    setStep('form')
  }

  async function onPay() {
    if (!data) return
    await acceptQuote(data.quoteId).unwrap().catch(() => {})
    setStep('confirmed')
  }

  async function onCancelAtPayment() {
    if (!data) return
    await cancelQuote(data.quoteId).catch(() => {})
    setStep('form')
  }

  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Get a premium quote</h2>
      <p className="mt-1 text-sm text-ink-soft">
        {prefill
          ? <>Pricing coverage for policy <span className="font-mono font-semibold text-ink">{prefill.policyNumber}</span> - accepting will update this policy's premium.</>
          : 'Runs the live dynamic-pricing engine: duration, risk, location, usage & surge multipliers, then any coupon.'}
      </p>

      {step === 'form' && (
        <>
          <form onSubmit={onSubmit} className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-3">
            <Field label="Base premium ($)">
              <input type="number" step="0.01" className={inputClass} value={form.basePremium}
                onChange={(e) => setForm({ ...form, basePremium: Number(e.target.value) })} required />
            </Field>
            <Field label="Duration (hours)">
              <input type="number" className={inputClass} value={form.durationHours}
                onChange={(e) => setForm({ ...form, durationHours: Number(e.target.value) })} required />
            </Field>
            <Field label="Risk category">
              <select className={inputClass} value={form.riskCategory} onChange={(e) => setForm({ ...form, riskCategory: e.target.value })}>
                <option>LOW</option><option>MEDIUM</option><option>HIGH</option>
              </select>
            </Field>
            <Field label="Location">
              <select className={inputClass} value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })}>
                <option>URBAN</option><option>RURAL</option>
              </select>
            </Field>
            <Field label="Usage level">
              <select className={inputClass} value={form.usageLevel} onChange={(e) => setForm({ ...form, usageLevel: e.target.value })}>
                <option>LIGHT</option><option>MODERATE</option><option>HEAVY</option>
              </select>
            </Field>
            <Field label="Coupon code (optional)">
              <input className={inputClass} value={form.couponCode}
                onChange={(e) => setForm({ ...form, couponCode: e.target.value.toUpperCase() })} placeholder="WELCOME10" />
            </Field>
            <div className="col-span-full">
              <Button type="submit" disabled={isLoading}>{isLoading ? 'Calculating…' : 'Get quote'}</Button>
            </div>
          </form>
          {error && <p className="mt-3 text-sm text-danger">Couldn't price that quote - check the coupon code and inputs.</p>}
        </>
      )}

      {step === 'review' && data && (
        <div className="mt-5 rounded-xl bg-ultra-soft p-4">
          <div className="flex items-baseline justify-between">
            <span className="text-sm text-ink-soft">Final premium</span>
            <span className="font-display text-2xl font-bold text-ultra"><Money value={data.finalPremium} /></span>
          </div>
          <div className="mt-2 grid grid-cols-2 gap-2 text-xs text-ink-soft">
            <div>Before discount <span className="font-mono text-ink"><Money value={data.premiumBeforeDiscount} /></span></div>
            <div>Discount <span className="font-mono text-ink"><Money value={data.discountApplied} /></span></div>
            <div>Multiplier <span className="font-mono text-ink">×{(data.multiplierApplied ?? 0).toFixed(3)}</span></div>
          </div>
          {(data.appliedRules?.length ?? 0) > 0 && (
            <ul className="mt-3 flex flex-wrap gap-1.5">
              {data.appliedRules.map((r, i) => (
                <li key={i} className="rounded-full bg-surface px-2 py-1 text-[11px] font-mono text-ink-soft">{r}</li>
              ))}
            </ul>
          )}
          <div className="mt-4 flex items-center gap-3 border-t border-line pt-4">
            <p className="flex-1 text-sm font-semibold text-ink">Do you want to proceed with this coverage?</p>
            <Button variant="ghost" onClick={onNotNow}>Not now</Button>
            <Button onClick={() => setStep('payment')}>Proceed</Button>
          </div>
        </div>
      )}

      {step === 'payment' && data && (
        <div className="mt-5 rounded-xl border border-line bg-surface p-4">
          <div className="text-center">
            <div className="text-xs font-semibold uppercase tracking-wide text-ink-soft">Amount due</div>
            <div className="font-display text-3xl font-bold text-ink"><Money value={data.finalPremium} /></div>
          </div>
          <div className="mt-4 flex justify-center gap-3">
            <Button variant="ghost" onClick={onCancelAtPayment} disabled={paying}>Cancel</Button>
            <Button onClick={onPay} disabled={paying}>{paying ? 'Processing…' : 'Pay'}</Button>
          </div>
        </div>
      )}

      {step === 'confirmed' && data && (
        <div className="mt-5 rounded-xl bg-mint-soft p-5 text-center">
          <div className="font-display text-lg font-semibold text-mint">Coverage confirmed ✓</div>
          <p className="mt-1 text-sm text-ink-soft">
            <Money value={data.finalPremium} /> charged{prefill ? <> for policy <span className="font-mono">{prefill.policyNumber}</span></> : null}.
            {prefill ? ' The policy is now active and this shows up in its premium history.' : ''}
          </p>
          <Button className="mt-4" variant="ghost" onClick={() => setStep('form')}>Get another quote</Button>
        </div>
      )}
    </Card>
  )
}

function CouponsPanel() {
  const userId = useEnsureUserId()
  const { roles } = useAppSelector((s) => s.auth)
  const isAdmin = roles.includes('ROLE_ADMIN')
  const { data: coupons } = useCouponsQuery()
  const [createCoupon, { isLoading }] = useCreateCouponMutation()
  const [form, setForm] = useState({ code: '', discountPercent: 10, maxRedemptions: 100 })
  const [checkCode, setCheckCode] = useState('')
  const [validate, { data: validation, isFetching: checking }] = useLazyValidateCouponQuery()

  async function onCreate(e: React.FormEvent) {
    e.preventDefault()
    const now = new Date()
    const nextYear = new Date(now.getFullYear() + 1, now.getMonth(), now.getDate())
    await createCoupon({
      code: form.code.toUpperCase(),
      discountPercent: form.discountPercent,
      maxRedemptions: form.maxRedemptions,
      validFrom: now.toISOString(),
      validUntil: nextYear.toISOString(),
    }).unwrap().catch(() => {})
    setForm({ code: '', discountPercent: 10, maxRedemptions: 100 })
  }

  function onCheck(e: React.FormEvent) {
    e.preventDefault()
    if (!checkCode || !userId) return
    validate({ code: checkCode.toUpperCase(), userId })
  }

  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Coupons</h2>
      {isAdmin && (
        <form onSubmit={onCreate} className="mt-3 flex flex-wrap items-end gap-3 border-b border-line pb-4">
          <Field label="Code">
            <input className={inputClass + ' w-32'} value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} required />
          </Field>
          <Field label="Discount %">
            <input type="number" className={inputClass + ' w-24'} value={form.discountPercent}
              onChange={(e) => setForm({ ...form, discountPercent: Number(e.target.value) })} min={0} max={100} />
          </Field>
          <Field label="Max redemptions">
            <input type="number" className={inputClass + ' w-28'} value={form.maxRedemptions}
              onChange={(e) => setForm({ ...form, maxRedemptions: Number(e.target.value) })} min={1} />
          </Field>
          <Button type="submit" disabled={isLoading}>Add coupon</Button>
        </form>
      )}

      <form onSubmit={onCheck} className="mt-3 flex flex-wrap items-end gap-3 border-b border-line pb-4">
        <Field label="Check a code before using it">
          <input className={inputClass + ' w-32'} value={checkCode}
            onChange={(e) => setCheckCode(e.target.value.toUpperCase())} placeholder="WELCOME10" />
        </Field>
        <Button type="submit" variant="ghost" disabled={checking || !checkCode}>{checking ? 'Checking…' : 'Check code'}</Button>
        {validation && (
          <span className={`text-xs font-semibold ${validation.valid ? 'text-mint' : 'text-danger'}`}>
            {validation.valid ? '✓ Valid' : `✗ ${validation.message}`}
          </span>
        )}
      </form>

      <div className="mt-4 space-y-2">
        {!coupons?.length ? (
          <p className="text-sm text-ink-soft">No coupons yet.</p>
        ) : coupons.map((c) => (
          <div key={c.id} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm">
            <div>
              <span className="font-mono font-semibold text-ink">{c.code}</span>
              <span className="ml-2 text-ink-soft">{c.discountPercent}% off</span>
            </div>
            <div className="text-xs text-ink-soft">{c.timesRedeemed}/{c.maxRedemptions} used</div>
          </div>
        ))}
      </div>
    </Card>
  )
}

function UsagePanel() {
  const userId = useEnsureUserId()
  const { data: policies } = usePoliciesByUserQuery(userId ?? 0, { skip: !userId })
  const activePolicies = policies?.filter((p) => p.status === 'ACTIVE') ?? []
  const [form, setForm] = useState({ policyId: '', usageType: 'HOURS', quantity: 1 })
  const [recordUsage, { isLoading }] = useRecordUsageMutation()
  const [lastResult, setLastResult] = useState<UsageResponse | null>(null)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!userId || !form.policyId) return
    const result = await recordUsage({ policyId: Number(form.policyId), userId, usageType: form.usageType, quantity: form.quantity }).unwrap()
    setLastResult(result)
  }

  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Log usage</h2>
      <p className="mt-1 text-sm text-ink-soft">Feeds real-time telemetry &amp; triggers 80%/100% usage-cap alerts.</p>
      {!activePolicies.length ? (
        <EmptyState title="No active policies" hint="Activate a policy first to log usage against it." />
      ) : (
        <form onSubmit={onSubmit} className="mt-4 flex flex-wrap items-end gap-4">
          <Field label="Policy">
            <select className={inputClass} value={form.policyId} onChange={(e) => setForm({ ...form, policyId: e.target.value })} required>
              <option value="" disabled>Choose…</option>
              {activePolicies.map((p) => <option key={p.id} value={p.id}>{p.policyNumber}</option>)}
            </select>
          </Field>
          <Field label="Type">
            <input className={inputClass + ' w-32'} value={form.usageType} onChange={(e) => setForm({ ...form, usageType: e.target.value })} />
          </Field>
          <Field label="Quantity">
            <input type="number" className={inputClass + ' w-24'} value={form.quantity}
              onChange={(e) => setForm({ ...form, quantity: Number(e.target.value) })} min={0.1} step={0.1} />
          </Field>
          <Button type="submit" disabled={isLoading}>Log usage</Button>
          {lastResult && !lastResult.thresholdCrossed && (
            <span className="text-xs font-semibold text-mint">Recorded ✓ ({lastResult.totalUsage}/{lastResult.usageCap ?? '—'})</span>
          )}
        </form>
      )}

      {lastResult?.thresholdCrossed && (
        <div className={`mt-4 rounded-xl border px-4 py-3 ${
          lastResult.thresholdCrossed === 'CAP_REACHED'
            ? 'border-danger bg-danger-soft'
            : 'border-amber bg-amber-soft'
        }`}>
          <div className={`flex items-center gap-2 font-display text-sm font-semibold ${
            lastResult.thresholdCrossed === 'CAP_REACHED' ? 'text-danger' : 'text-amber'
          }`}>
            <span className="live-dot text-lg leading-none">●</span>
            {lastResult.thresholdCrossed === 'CAP_REACHED'
              ? 'Usage cap reached!'
              : 'Usage warning: 80% of cap reached'}
          </div>
          <p className="mt-1 text-xs text-ink-soft">
            {lastResult.totalUsage} / {lastResult.usageCap ?? '—'} used
            ({lastResult.percentage.toFixed(1)}%) - an alert was sent for this policy.
          </p>
        </div>
      )}
    </Card>
  )
}

export default function PricingPage() {
  const location = useLocation() as {
    state?: { policyId: number; policyNumber: string; basePremium: number; riskCategory: string; durationHours: number }
  }
  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl font-bold text-ink">Pricing &amp; Coupons</h1>
        <p className="mt-1 text-ink-soft">The dynamic pricing engine, discount system, and usage telemetry.</p>
      </div>
      <QuoteTool prefill={location.state} />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <CouponsPanel />
        <UsagePanel />
      </div>
    </div>
  )
}
