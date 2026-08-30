import { useState, type ReactNode } from 'react'

export function Card({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <div className={`bg-surface border border-line rounded-2xl p-5 shadow-[0_1px_2px_rgba(18,23,43,0.04)] ${className}`}>
      {children}
    </div>
  )
}

export function StatCard({ label, value, sub }: { label: string; value: ReactNode; sub?: string }) {
  return (
    <Card>
      <div className="text-xs font-medium uppercase tracking-wide text-ink-soft">{label}</div>
      <div className="mt-1 font-display text-3xl font-semibold text-ink">{value}</div>
      {sub && <div className="mt-1 text-xs text-ink-soft">{sub}</div>}
    </Card>
  )
}

const statusStyles: Record<string, string> = {
  ACTIVE: 'bg-mint-soft text-mint',
  DRAFT: 'bg-ultra-soft text-ultra',
  EXPIRED: 'bg-line text-ink-soft',
  CANCELLED: 'bg-danger-soft text-danger',
  SUBMITTED: 'bg-ultra-soft text-ultra',
  VALIDATED: 'bg-mint-soft text-mint',
  UNDER_REVIEW: 'bg-amber-soft text-amber',
  ON_HOLD: 'bg-danger-soft text-danger',
  APPROVED: 'bg-mint-soft text-mint',
  REJECTED: 'bg-danger-soft text-danger',
}

export function StatusPill({ status }: { status: string }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ${statusStyles[status] ?? 'bg-line text-ink-soft'}`}>
      {status.replace('_', ' ')}
    </span>
  )
}

export function Button({
  children, onClick, variant = 'primary', type = 'button', disabled, className = '',
}: {
  children: ReactNode
  onClick?: () => void
  variant?: 'primary' | 'ghost' | 'danger'
  type?: 'button' | 'submit'
  disabled?: boolean
  className?: string
}) {
  const base = 'inline-flex items-center justify-center rounded-xl px-4 py-2 text-sm font-semibold transition disabled:opacity-40 disabled:cursor-not-allowed'
  const styles = {
    primary: 'bg-flare text-white hover:brightness-95 active:brightness-90',
    ghost: 'bg-transparent text-ink border border-line hover:bg-ultra-soft',
    danger: 'bg-danger-soft text-danger hover:bg-danger hover:text-white',
  }[variant]
  return (
    <button type={type} onClick={onClick} disabled={disabled} className={`${base} ${styles} ${className}`}>
      {children}
    </button>
  )
}

export function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-ink-soft">{label}</span>
      {children}
    </label>
  )
}

export const inputClass =
  'w-full rounded-xl border border-line bg-paper px-3 py-2 text-sm text-ink outline-none focus:border-ultra focus:ring-2 focus:ring-ultra-soft'

export function PasswordField({
  value, onChange, placeholder, autoComplete,
}: {
  value: string
  onChange: (v: string) => void
  placeholder?: string
  autoComplete?: string
}) {
  const [visible, setVisible] = useState(false)
  return (
    <div className="relative">
      <input
        className={inputClass + ' pr-10'}
        type={visible ? 'text' : 'password'}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        autoComplete={autoComplete}
        required
      />
      <button
        type="button"
        onClick={() => setVisible((v) => !v)}
        className="absolute right-2 top-1/2 -translate-y-1/2 text-ink-soft hover:text-ink"
        aria-label={visible ? 'Hide password' : 'Show password'}
      >
        {visible ? (
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a18.5 18.5 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
            <line x1="1" y1="1" x2="23" y2="23" />
          </svg>
        ) : (
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        )}
      </button>
    </div>
  )
}

const PASSWORD_RULES: { label: string; test: (pw: string) => boolean }[] = [
  { label: 'At least 6 characters', test: (pw) => pw.length >= 6 },
  { label: 'One uppercase letter', test: (pw) => /[A-Z]/.test(pw) },
  { label: 'One lowercase letter', test: (pw) => /[a-z]/.test(pw) },
  { label: 'One number', test: (pw) => /\d/.test(pw) },
  { label: 'One special character', test: (pw) => /[^A-Za-z0-9]/.test(pw) },
]

export function passwordMeetsAllRules(pw: string): boolean {
  return PASSWORD_RULES.every((r) => r.test(pw))
}

export function PasswordStrengthChecklist({ password }: { password: string }) {
  return (
    <ul className="mt-2 grid grid-cols-1 gap-1 sm:grid-cols-2">
      {PASSWORD_RULES.map((rule) => {
        const met = rule.test(password)
        return (
          <li key={rule.label} className={`flex items-center gap-1.5 text-xs ${met ? 'text-mint' : 'text-ink-soft'}`}>
            <span className={`flex h-4 w-4 items-center justify-center rounded-full text-[10px] ${met ? 'bg-mint text-white' : 'bg-line text-ink-soft'}`}>
              {met ? '✓' : ''}
            </span>
            {rule.label}
          </li>
        )
      })}
    </ul>
  )
}

export function Money({ value }: { value: number | null | undefined }) {
  return <span className="font-mono">${(value ?? 0).toFixed(2)}</span>
}

export function CoverageRing({ pct, label }: { pct: number; label: string }) {
  const clamped = Math.max(0, Math.min(100, pct))
  return (
    <div className="flex items-center gap-3">
      <div
        className="coverage-ring relative h-12 w-12 shrink-0 rounded-full"
        style={{ '--pct': clamped } as React.CSSProperties}
      >
        <div className="absolute inset-1 rounded-full bg-surface flex items-center justify-center text-[10px] font-mono font-semibold text-ink">
          {Math.round(clamped)}%
        </div>
      </div>
      <div className="text-xs text-ink-soft">{label}</div>
    </div>
  )
}

export function EmptyState({ title, hint }: { title: string; hint?: string }) {
  return (
    <div className="rounded-2xl border border-dashed border-line py-12 text-center">
      <div className="font-display text-lg font-semibold text-ink">{title}</div>
      {hint && <div className="mt-1 text-sm text-ink-soft">{hint}</div>}
    </div>
  )
}
