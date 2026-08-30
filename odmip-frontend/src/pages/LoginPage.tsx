import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useLoginMutation } from '../api/userApi'
import { useAppDispatch } from '../app/hooks'
import { credentialsSet } from '../features/auth/authSlice'
import { Button, Field, PasswordField, inputClass } from '../components/ui'

type LoginRole = 'ADMIN' | 'USER'

export default function LoginPage() {
  const location = useLocation() as { state?: { justVerified?: boolean } }
  const [role, setRole] = useState<LoginRole | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [roleMismatch, setRoleMismatch] = useState(false)
  const [login, { isLoading, error }] = useLoginMutation()
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setRoleMismatch(false)
    try {
      const res = await login({ username, password }).unwrap()
      const isAdminAccount = res.roles.includes('ROLE_ADMIN')
      if (role === 'ADMIN' && !isAdminAccount) {
        setRoleMismatch(true)
        return
      }
      if (role === 'USER' && isAdminAccount) {
        setRoleMismatch(true)
        return
      }
      dispatch(credentialsSet(res))
      navigate('/')
    } catch {
      // error state below handles display
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-paper px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <div className="font-display text-2xl font-bold text-ink">OD·MIP</div>
          <div className="mt-1 text-sm text-ink-soft">Coverage that starts and stops when you do.</div>
        </div>

        {location.state?.justVerified && (
          <div className="mb-4 rounded-xl bg-mint-soft px-4 py-3 text-center text-sm font-semibold text-mint">
            Email verified! Sign in to continue.
          </div>
        )}

        {!role ? (
          <div className="rounded-2xl border border-line bg-surface p-6 shadow-sm">
            <p className="mb-4 text-center text-sm font-semibold text-ink-soft">Sign in as</p>
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => setRole('ADMIN')}
                className="rounded-xl border-2 border-line p-5 text-center transition hover:border-ultra hover:bg-ultra-soft"
              >
                <div className="text-2xl">⚑</div>
                <div className="mt-2 font-display font-semibold text-ink">Admin</div>
              </button>
              <button
                onClick={() => setRole('USER')}
                className="rounded-xl border-2 border-line p-5 text-center transition hover:border-ultra hover:bg-ultra-soft"
              >
                <div className="text-2xl">◈</div>
                <div className="mt-2 font-display font-semibold text-ink">User</div>
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={onSubmit} className="space-y-4 rounded-2xl border border-line bg-surface p-6 shadow-sm">
            <button
              type="button"
              onClick={() => { setRole(null); setRoleMismatch(false) }}
              className="mb-1 text-xs font-semibold text-ink-soft hover:text-ink"
            >
              ← Signing in as {role === 'ADMIN' ? 'Admin' : 'User'} · change
            </button>
            <Field label="Username">
              <input className={inputClass} value={username} onChange={(e) => setUsername(e.target.value)} required autoFocus />
            </Field>
            <Field label="Password">
              <PasswordField value={password} onChange={setPassword} autoComplete="current-password" />
            </Field>
            {roleMismatch && (
              <p className="text-sm text-danger">
                {role === 'ADMIN'
                  ? "This account isn't an admin account - try the User tab instead."
                  : 'This is an admin account - use the Admin tab instead.'}
              </p>
            )}
            {error && !roleMismatch && <p className="text-sm text-danger">Invalid username or password.</p>}
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
        )}

        <p className="mt-4 text-center text-sm text-ink-soft">
          New here? <Link to="/register" className="font-semibold text-ultra">Create an account</Link>
        </p>
      </div>
    </div>
  )
}
