import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useEnsureUserId } from '../app/useEnsureUserId'
import {
  useActivatePolicyMutation, useCancelPolicyMutation, useCreatePolicyMutation,
  usePoliciesByUserQuery, usePremiumHistoryQuery, useTemplatesQuery,
} from '../api/userApi'
import { Button, Card, CoverageRing, EmptyState, Field, inputClass, Money, StatusPill } from '../components/ui'
import type { Policy } from '../types'

function timeRemainingPct(p: Policy): number {
  const start = new Date(p.startDate).getTime()
  const end = new Date(p.endDate).getTime()
  const now = Date.now()
  if (now >= end) return 0
  if (now <= start) return 100
  return ((end - now) / (end - start)) * 100
}

function PolicyCard({ policy }: { policy: Policy }) {
  const navigate = useNavigate()
  const [activate, { isLoading: activating }] = useActivatePolicyMutation()
  const [cancel, { isLoading: cancelling }] = useCancelPolicyMutation()
  const [showHistory, setShowHistory] = useState(false)
  const { data: history } = usePremiumHistoryQuery(policy.id, { skip: !showHistory })

  const pct = policy.status === 'ACTIVE' ? timeRemainingPct(policy) : policy.status === 'DRAFT' ? 100 : 0

  function getQuoteForThisPolicy() {
    const durationHours = Math.round((new Date(policy.endDate).getTime() - new Date(policy.startDate).getTime()) / 3_600_000)
    navigate('/pricing', {
      state: {
        policyId: policy.id,
        policyNumber: policy.policyNumber,
        basePremium: policy.premiumAmount,
        riskCategory: policy.template?.riskCategory ?? 'MEDIUM',
        durationHours: durationHours > 0 ? durationHours : 24,
      },
    })
  }

  return (
    <Card>
      <div className="flex items-start justify-between">
        <div>
          <div className="font-mono text-sm font-semibold text-ink">{policy.policyNumber}</div>
          <div className="text-sm text-ink-soft">{policy.template?.name}</div>
        </div>
        <StatusPill status={policy.status} />
      </div>

      <div className="mt-4 flex items-center justify-between">
        <CoverageRing
          pct={pct}
          label={policy.status === 'ACTIVE' ? 'coverage remaining' : policy.status === 'DRAFT' ? 'ready to activate' : 'closed'}
        />
        <div className="text-right">
          <div className="text-xs text-ink-soft">Premium</div>
          <div className="font-display text-lg font-semibold"><Money value={policy.premiumAmount} /></div>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-3 text-xs text-ink-soft">
        <div>Coverage <div className="font-mono text-ink"><Money value={policy.coverageAmount} /></div></div>
        <div>Risk <div className="font-mono text-ink">{policy.template?.riskCategory}</div></div>
        <div>Starts <div className="text-ink">{new Date(policy.startDate).toLocaleString()}</div></div>
        <div>Ends <div className="text-ink">{new Date(policy.endDate).toLocaleString()}</div></div>
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        {policy.status === 'DRAFT' && (
          <Button onClick={() => activate(policy.id)} disabled={activating}>Activate</Button>
        )}
        {(policy.status === 'DRAFT' || policy.status === 'ACTIVE') && (
          <Button variant="danger" onClick={() => cancel(policy.id)} disabled={cancelling}>Cancel</Button>
        )}
        <Button variant="ghost" onClick={() => setShowHistory((v) => !v)}>
          {showHistory ? 'Hide' : 'Show'} premium history
        </Button>
        {(policy.status === 'DRAFT' || policy.status === 'ACTIVE') && (
          <Button variant="ghost" onClick={getQuoteForThisPolicy}>Get quote for this policy</Button>
        )}
      </div>

      {showHistory && (
        <div className="mt-3 border-t border-line pt-3">
          {!history?.length ? (
            <p className="text-xs text-ink-soft">No premium changes recorded yet.</p>
          ) : (
            <ul className="space-y-1 text-xs">
              {history.map((h) => (
                <li key={h.id} className="flex justify-between text-ink-soft">
                  <span>{new Date(h.changedAt).toLocaleString()}</span>
                  <span className="font-mono text-ink"><Money value={h.premiumAmount} /></span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </Card>
  )
}

export default function PoliciesPage() {
  const userId = useEnsureUserId()
  const { data: policies, isLoading } = usePoliciesByUserQuery(userId ?? 0, { skip: !userId })
  const { data: templates } = useTemplatesQuery()
  const [createPolicy, { isLoading: creating }] = useCreatePolicyMutation()
  const [templateCode, setTemplateCode] = useState('')

  async function onCreate(e: React.FormEvent) {
    e.preventDefault()
    if (!userId || !templateCode) return
    await createPolicy({ userId, templateCode }).unwrap()
    setTemplateCode('')
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl font-bold text-ink">Policies</h1>
        <p className="mt-1 text-ink-soft">Buy coverage instantly for exactly as long as you need it.</p>
      </div>

      <Card>
        <h2 className="font-display text-lg font-semibold text-ink">Buy a new policy</h2>
        <form onSubmit={onCreate} className="mt-4 flex flex-wrap items-end gap-4">
          <div className="min-w-64">
            <Field label="Template">
              <select className={inputClass} value={templateCode} onChange={(e) => setTemplateCode(e.target.value)} required>
                <option value="" disabled>Choose a template…</option>
                {templates?.map((t) => (
                  <option key={t.code} value={t.code}>
                    {t.name} — {t.defaultDurationHours}h — ${t.basePremium}
                  </option>
                ))}
              </select>
            </Field>
          </div>
          <Button type="submit" disabled={creating || !templateCode}>
            {creating ? 'Creating…' : 'Create policy (DRAFT)'}
          </Button>
        </form>
      </Card>

      <div>
        <h2 className="mb-3 font-display text-lg font-semibold text-ink">Your policies</h2>
        {isLoading ? (
          <div className="text-ink-soft">Loading…</div>
        ) : !policies?.length ? (
          <EmptyState title="No policies yet" hint="Create one above to get started." />
        ) : (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {policies.map((p) => <PolicyCard key={p.id} policy={p} />)}
          </div>
        )}
      </div>
    </div>
  )
}
