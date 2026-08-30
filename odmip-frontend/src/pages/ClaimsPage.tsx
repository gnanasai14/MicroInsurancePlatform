import { useState } from 'react'
import { useAppSelector } from '../app/hooks'
import { useEnsureUserId } from '../app/useEnsureUserId'
import {
  useClaimsByUserQuery, useFraudFlagsQuery, useRiskScoreQuery,
  useSubmitClaimMutation, useUpdateClaimStatusMutation,
} from '../api/claimsApi'
import { usePoliciesByUserQuery } from '../api/userApi'
import { Button, Card, EmptyState, Field, inputClass, Money, StatusPill } from '../components/ui'
import type { Claim, ClaimStatus } from '../types'

const NEXT_STATUS: Record<ClaimStatus, ClaimStatus[]> = {
  SUBMITTED: ['VALIDATED', 'ON_HOLD', 'REJECTED'],
  VALIDATED: ['UNDER_REVIEW', 'ON_HOLD', 'REJECTED'],
  UNDER_REVIEW: ['APPROVED', 'REJECTED', 'ON_HOLD'],
  ON_HOLD: ['UNDER_REVIEW', 'REJECTED'],
  APPROVED: [],
  REJECTED: [],
}

function ClaimRow({ claim, canReview }: { claim: Claim; canReview: boolean }) {
  const [expanded, setExpanded] = useState(false)
  const { data: risk } = useRiskScoreQuery(claim.id, { skip: !expanded })
  const { data: flags } = useFraudFlagsQuery(claim.id, { skip: !expanded })
  const [updateStatus, { isLoading }] = useUpdateClaimStatusMutation()

  const nextOptions = NEXT_STATUS[claim.status] ?? []

  return (
    <Card>
      <button className="flex w-full items-center justify-between text-left" onClick={() => setExpanded((v) => !v)}>
        <div>
          <div className="font-mono text-sm font-semibold text-ink">{claim.claimNumber}</div>
          <div className="text-xs text-ink-soft">Policy #{claim.policyId} · {new Date(claim.submittedAt).toLocaleDateString()}</div>
        </div>
        <div className="flex items-center gap-3">
          <span className="font-display text-lg font-semibold"><Money value={claim.claimedAmount} /></span>
          <StatusPill status={claim.status} />
        </div>
      </button>

      {expanded && (
        <div className="mt-4 space-y-3 border-t border-line pt-4">
          {claim.description && <p className="text-sm text-ink-soft">{claim.description}</p>}
          <div className="flex flex-wrap gap-4 text-xs text-ink-soft">
            <span>Policy validated: <strong className="text-ink">{claim.policyValidated ? 'Yes' : 'No'}</strong></span>
            {risk && <span>Risk score: <strong className="text-ink">{risk.score}</strong></span>}
          </div>
          {!!flags?.length && (
            <div className="rounded-lg bg-danger-soft p-3">
              <div className="text-xs font-semibold text-danger">Fraud flags raised</div>
              <ul className="mt-1 space-y-0.5">
                {flags.map((f) => (
                  <li key={f.id} className="text-xs text-danger">• {f.ruleTriggered}: {f.reason}</li>
                ))}
              </ul>
            </div>
          )}
          {canReview && nextOptions.length > 0 && (
            <div className="flex flex-wrap gap-2 pt-1">
              {nextOptions.map((s) => (
                <Button
                  key={s}
                  variant={s === 'REJECTED' || s === 'ON_HOLD' ? 'danger' : 'ghost'}
                  disabled={isLoading}
                  onClick={() => updateStatus({ id: claim.id, newStatus: s })}
                >
                  Mark {s.replace('_', ' ').toLowerCase()}
                </Button>
              ))}
            </div>
          )}
        </div>
      )}
    </Card>
  )
}

export default function ClaimsPage() {
  const userId = useEnsureUserId()
  const { roles } = useAppSelector((s) => s.auth)
  const canReview = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_UNDERWRITER')

  const { data: claims, isLoading } = useClaimsByUserQuery(userId ?? 0, { skip: !userId })
  const { data: policies } = usePoliciesByUserQuery(userId ?? 0, { skip: !userId })
  const [submitClaim, { isLoading: submitting, error }] = useSubmitClaimMutation()
  const [form, setForm] = useState({ policyId: '', claimedAmount: 100, description: '' })

  const eligiblePolicies = policies?.filter((p) => p.status === 'ACTIVE') ?? []

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!userId || !form.policyId) return
    await submitClaim({
      policyId: Number(form.policyId), userId, claimedAmount: form.claimedAmount, description: form.description || undefined,
    }).unwrap().catch(() => {})
    setForm({ policyId: '', claimedAmount: 100, description: '' })
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl font-bold text-ink">Claims</h1>
        <p className="mt-1 text-ink-soft">Submit, track, and review claims - validated live against your active policies.</p>
      </div>

      <Card>
        <h2 className="font-display text-lg font-semibold text-ink">Submit a claim</h2>
        {!eligiblePolicies.length ? (
          <EmptyState title="No active policies" hint="You need an ACTIVE policy to file a claim against." />
        ) : (
          <form onSubmit={onSubmit} className="mt-4 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Field label="Policy">
                <select className={inputClass} value={form.policyId} onChange={(e) => setForm({ ...form, policyId: e.target.value })} required>
                  <option value="" disabled>Choose…</option>
                  {eligiblePolicies.map((p) => <option key={p.id} value={p.id}>{p.policyNumber}</option>)}
                </select>
              </Field>
              <Field label="Claimed amount ($)">
                <input type="number" step="0.01" className={inputClass} value={form.claimedAmount}
                  onChange={(e) => setForm({ ...form, claimedAmount: Number(e.target.value) })} required />
              </Field>
            </div>
            <Field label="Description">
              <textarea className={inputClass} rows={2} value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })} maxLength={1000} />
            </Field>
            {error && <p className="text-sm text-danger">Couldn't submit that claim - the policy may not be ACTIVE.</p>}
            <Button type="submit" disabled={submitting}>{submitting ? 'Submitting…' : 'Submit claim'}</Button>
          </form>
        )}
      </Card>

      <div>
        <h2 className="mb-3 font-display text-lg font-semibold text-ink">Your claims</h2>
        {isLoading ? (
          <div className="text-ink-soft">Loading…</div>
        ) : !claims?.length ? (
          <EmptyState title="No claims filed yet" />
        ) : (
          <div className="space-y-3">
            {claims.map((c) => <ClaimRow key={c.id} claim={c} canReview={canReview} />)}
          </div>
        )}
      </div>
    </div>
  )
}
