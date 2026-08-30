import { Link } from 'react-router-dom'
import { useAppSelector } from '../app/hooks'
import { useEnsureUserId } from '../app/useEnsureUserId'
import { useDashboardQuery } from '../api/pricingApi'
import { usePoliciesByUserQuery } from '../api/userApi'
import { Card, EmptyState, Money, StatCard, StatusPill } from '../components/ui'

export default function DashboardPage() {
  const { username } = useAppSelector((s) => s.auth)
  const userId = useEnsureUserId()
  const { data: dash, isLoading: dashLoading } = useDashboardQuery(userId ?? 0, { skip: !userId })
  const { data: policies } = usePoliciesByUserQuery(userId ?? 0, { skip: !userId })

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl font-bold text-ink">Welcome back, {username}</h1>
        <p className="mt-1 text-ink-soft">Here's where your coverage stands right now.</p>
      </div>

      {dashLoading ? (
        <div className="text-ink-soft">Loading dashboard…</div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <StatCard label="Active policies" value={dash?.activePolicyCount ?? 0} sub={`${dash?.totalPolicyCount ?? 0} total`} />
          <StatCard label="Total premium paid" value={<Money value={dash?.totalPremiumPaid} />} />
          <StatCard
            label="Usage logged"
            value={dash?.policyUsage.reduce((s, p) => s + p.totalUsage, 0).toFixed(1) ?? 0}
            sub="across all policies"
          />
        </div>
      )}

      <div>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-display text-lg font-semibold text-ink">Your policies</h2>
          <Link to="/policies" className="text-sm font-semibold text-ultra">Manage policies →</Link>
        </div>
        {!policies?.length ? (
          <EmptyState title="No policies yet" hint="Buy your first on-demand policy from the Policies tab." />
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {policies.slice(0, 4).map((p) => (
              <Card key={p.id} className="flex items-center justify-between">
                <div>
                  <div className="font-mono text-sm font-semibold text-ink">{p.policyNumber}</div>
                  <div className="text-xs text-ink-soft">{p.template?.name}</div>
                  <div className="mt-2"><Money value={p.premiumAmount} /></div>
                </div>
                <StatusPill status={p.status} />
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
