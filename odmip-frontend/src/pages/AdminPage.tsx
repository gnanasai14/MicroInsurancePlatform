import { useState } from 'react'
import {
  useAdminPoliciesQuery, useAdminUsersQuery, useDisableUserMutation,
} from '../api/userApi'
import {
  useAcceptedQuotesQuery, useAnalyticsSummaryQuery, useCreatePricingRuleMutation,
  useDeletePricingRuleMutation, usePricingRulesQuery, useUpdatePricingRuleMutation,
} from '../api/pricingApi'
import { useCreateFraudRuleMutation, useFraudRulesQuery } from '../api/claimsApi'
import { Button, Card, Field, inputClass, Money, StatusPill } from '../components/ui'
import type { PricingRule } from '../types'

function UsersPanel() {
  const { data: users } = useAdminUsersQuery()
  const [disableUser] = useDisableUserMutation()
  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Users</h2>
      <div className="mt-3 space-y-2">
        {users?.map((u) => (
          <div key={u.id} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm">
            <div>
              <span className="font-semibold text-ink">{u.username}</span>
              <span className="ml-2 text-ink-soft">{u.email}</span>
            </div>
            <div className="flex items-center gap-2">
              <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ${u.status === 'ACTIVE' ? 'bg-mint-soft text-mint' : 'bg-danger-soft text-danger'}`}>
                {u.status}
              </span>
              <Button variant="danger" onClick={() => disableUser(u.id)}>Disable</Button>
            </div>
          </div>
        ))}
      </div>
    </Card>
  )
}

function PoliciesPanel() {
  const { data: policies } = useAdminPoliciesQuery()
  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">All policies</h2>
      <div className="mt-3 max-h-96 space-y-2 overflow-y-auto">
        {policies?.map((p) => (
          <div key={p.id} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm">
            <span className="font-mono text-ink">{p.policyNumber}</span>
            <span className="text-ink-soft">user #{p.userId}</span>
            <StatusPill status={p.status} />
          </div>
        ))}
      </div>
    </Card>
  )
}

const emptyRuleForm = { type: 'RISK' as PricingRule['type'], matchValue: '', multiplier: 1.0, active: true }

function PricingRulesPanel() {
  const { data: rules } = usePricingRulesQuery()
  const [createRule, { isLoading: creating }] = useCreatePricingRuleMutation()
  const [updateRule] = useUpdatePricingRuleMutation()
  const [deleteRule] = useDeletePricingRuleMutation()
  const [form, setForm] = useState(emptyRuleForm)
  const [editingId, setEditingId] = useState<number | null>(null)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (editingId) {
      await updateRule({ id: editingId, body: form }).unwrap().catch(() => {})
      setEditingId(null)
    } else {
      await createRule(form).unwrap().catch(() => {})
    }
    setForm(emptyRuleForm)
  }

  function startEdit(rule: PricingRule) {
    setEditingId(rule.id)
    setForm({ type: rule.type, matchValue: rule.matchValue, multiplier: rule.multiplier, active: rule.active })
  }

  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Pricing rules</h2>
      <p className="mt-1 text-xs text-ink-soft">Drives the multipliers applied in every live quote.</p>
      <form onSubmit={onSubmit} className="mt-3 flex flex-wrap items-end gap-3 border-b border-line pb-4">
        <Field label="Type">
          <select className={inputClass} value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as PricingRule['type'] })}>
            <option value="RISK">RISK</option>
            <option value="LOCATION">LOCATION</option>
            <option value="USAGE">USAGE</option>
            <option value="SURGE">SURGE</option>
          </select>
        </Field>
        <Field label="Match value">
          <input className={inputClass + ' w-32'} value={form.matchValue}
            onChange={(e) => setForm({ ...form, matchValue: e.target.value })}
            placeholder={form.type === 'SURGE' ? '23:00-04:00' : 'HIGH'} required />
        </Field>
        <Field label="Multiplier">
          <input type="number" step="0.01" className={inputClass + ' w-24'} value={form.multiplier}
            onChange={(e) => setForm({ ...form, multiplier: Number(e.target.value) })} required />
        </Field>
        <Button type="submit" disabled={creating}>{editingId ? 'Save changes' : 'Add rule'}</Button>
        {editingId && (
          <Button variant="ghost" onClick={() => { setEditingId(null); setForm(emptyRuleForm) }}>Cancel edit</Button>
        )}
      </form>
      <div className="mt-3 space-y-2">
        {rules?.map((r) => (
          <div key={r.id} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm font-mono">
            <span>{r.type}: {r.matchValue} <span className="text-ink-soft">×{r.multiplier}</span></span>
            <div className="flex gap-2">
              <Button variant="ghost" onClick={() => startEdit(r)}>Edit</Button>
              <Button variant="danger" onClick={() => deleteRule(r.id)}>Delete</Button>
            </div>
          </div>
        ))}
      </div>
    </Card>
  )
}

function AnalyticsPanel() {
  const { data } = useAnalyticsSummaryQuery()
  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Analytics summary</h2>
      <p className="mt-1 text-xs text-ink-soft">Revenue by risk category and coupon redemption counts, from accepted quotes.</p>
      <div className="mt-4 grid grid-cols-2 gap-4">
        <div>
          <div className="text-xs font-semibold uppercase tracking-wide text-ink-soft">Revenue by risk</div>
          <ul className="mt-2 space-y-1">
            {data && Object.entries(data.revenueByRiskCategory).length > 0 ? (
              Object.entries(data.revenueByRiskCategory).map(([risk, amount]) => (
                <li key={risk} className="flex justify-between text-sm">
                  <span className="font-mono text-ink-soft">{risk}</span>
                  <span className="font-mono font-semibold text-ink"><Money value={amount} /></span>
                </li>
              ))
            ) : <li className="text-xs text-ink-soft">No accepted quotes yet.</li>}
          </ul>
        </div>
        <div>
          <div className="text-xs font-semibold uppercase tracking-wide text-ink-soft">Coupon redemptions</div>
          <ul className="mt-2 space-y-1">
            {data && Object.entries(data.couponRedemptions).length > 0 ? (
              Object.entries(data.couponRedemptions).map(([code, count]) => (
                <li key={code} className="flex justify-between text-sm">
                  <span className="font-mono text-ink-soft">{code}</span>
                  <span className="font-mono font-semibold text-ink">{count}×</span>
                </li>
              ))
            ) : <li className="text-xs text-ink-soft">No coupons redeemed yet.</li>}
          </ul>
        </div>
      </div>
    </Card>
  )
}

function PurchasedCoveragePanel() {
  const { data: quotes } = useAcceptedQuotesQuery()
  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Purchased coverage</h2>
      <p className="mt-1 text-xs text-ink-soft">Every quote a customer has actually paid for.</p>
      <div className="mt-3 max-h-96 space-y-2 overflow-y-auto">
        {!quotes?.length ? (
          <p className="text-xs text-ink-soft">No coverage purchased yet.</p>
        ) : quotes.map((q) => (
          <div key={q.id} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm">
            <div>
              <span className="text-ink-soft">Policy #{q.policyId ?? '—'} · user #{q.userId}</span>
              <div className="text-xs text-ink-soft">{q.decidedAt ? new Date(q.decidedAt).toLocaleString() : ''}</div>
            </div>
            <span className="font-mono font-semibold text-ink"><Money value={q.finalPremium} /></span>
          </div>
        ))}
      </div>
    </Card>
  )
}

function FraudRulesPanel() {
  const { data: rules } = useFraudRulesQuery()
  const [createRule, { isLoading }] = useCreateFraudRuleMutation()
  const [form, setForm] = useState({ ruleCode: '', conditionType: 'AMOUNT_THRESHOLD', thresholdAmount: 10000, riskScoreWeight: 40 })

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    await createRule({ ...form, active: true }).unwrap().catch(() => {})
    setForm({ ruleCode: '', conditionType: 'AMOUNT_THRESHOLD', thresholdAmount: 10000, riskScoreWeight: 40 })
  }

  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Fraud rules</h2>
      <form onSubmit={onSubmit} className="mt-3 flex flex-wrap items-end gap-3 border-b border-line pb-4">
        <Field label="Rule code">
          <input className={inputClass + ' w-40'} value={form.ruleCode} onChange={(e) => setForm({ ...form, ruleCode: e.target.value })} required />
        </Field>
        <Field label="Condition">
          <select className={inputClass} value={form.conditionType} onChange={(e) => setForm({ ...form, conditionType: e.target.value })}>
            <option value="AMOUNT_THRESHOLD">AMOUNT_THRESHOLD</option>
            <option value="HIGH_CLAIM_FREQUENCY">HIGH_CLAIM_FREQUENCY</option>
            <option value="MISSING_DESCRIPTION">MISSING_DESCRIPTION</option>
          </select>
        </Field>
        <Field label="Weight">
          <input type="number" className={inputClass + ' w-20'} value={form.riskScoreWeight}
            onChange={(e) => setForm({ ...form, riskScoreWeight: Number(e.target.value) })} />
        </Field>
        <Button type="submit" disabled={isLoading}>Add rule</Button>
      </form>
      <div className="mt-3 space-y-2">
        {rules?.map((r) => (
          <div key={r.id} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm">
            <span className="font-mono text-ink">{r.ruleCode}</span>
            <span className="text-xs text-ink-soft">{r.conditionType} · weight {r.riskScoreWeight}</span>
          </div>
        ))}
      </div>
    </Card>
  )
}

export default function AdminPage() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl font-bold text-ink">Admin panel</h1>
        <p className="mt-1 text-ink-soft">Manage users, policies, pricing rules and fraud rules across the platform.</p>
      </div>
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <UsersPanel />
        <PoliciesPanel />
        <PricingRulesPanel />
        <FraudRulesPanel />
        <AnalyticsPanel />
        <PurchasedCoveragePanel />
      </div>
    </div>
  )
}
