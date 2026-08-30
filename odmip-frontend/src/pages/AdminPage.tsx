import { useState } from 'react'
import {
  useAdminPoliciesQuery, useAdminPricingRulesQuery, useAdminUsersQuery, useDisableUserMutation,
} from '../api/userApi'
import { useCreateFraudRuleMutation, useFraudRulesQuery } from '../api/claimsApi'
import { Button, Card, Field, inputClass, StatusPill } from '../components/ui'

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

function PricingRulesPanel() {
  const { data: rules } = useAdminPricingRulesQuery()
  return (
    <Card>
      <h2 className="font-display text-lg font-semibold text-ink">Pricing rules</h2>
      <p className="mt-1 text-xs text-ink-soft">Proxied through user-service's admin endpoint to pricing-service.</p>
      <div className="mt-3 space-y-2">
        {rules?.map((r, i) => (
          <div key={i} className="flex items-center justify-between rounded-lg border border-line px-3 py-2 text-sm font-mono">
            <span>{String(r.type)}: {String(r.matchValue)}</span>
            <span>×{String(r.multiplier)}</span>
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
      </div>
    </div>
  )
}
