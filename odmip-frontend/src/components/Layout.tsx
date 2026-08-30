import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../app/hooks'
import { loggedOut } from '../features/auth/authSlice'

const navItems = [
  { to: '/', label: 'Dashboard', icon: '◈' },
  { to: '/policies', label: 'Policies', icon: '▤' },
  { to: '/pricing', label: 'Pricing & Coupons', icon: '◇' },
  { to: '/claims', label: 'Claims', icon: '✦' },
]

export default function Layout() {
  const { username, roles } = useAppSelector((s) => s.auth)
  const isAdmin = roles.includes('ROLE_ADMIN')
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 shrink-0 flex-col border-r border-line bg-ultra px-4 py-6 text-white">
        <div className="mb-8 px-2">
          <div className="font-display text-lg font-bold tracking-tight">OD·MIP</div>
          <div className="text-[11px] uppercase tracking-widest text-white/50">On-Demand Micro Insurance</div>
        </div>
        <nav className="flex flex-1 flex-col gap-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
                  isActive ? 'bg-white/15 text-white' : 'text-white/70 hover:bg-white/10 hover:text-white'
                }`
              }
            >
              <span className="text-base">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
          {isAdmin && (
            <NavLink
              to="/admin"
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
                  isActive ? 'bg-flare/90 text-white' : 'text-amber hover:bg-white/10'
                }`
              }
            >
              <span className="text-base">⚑</span>
              Admin Panel
            </NavLink>
          )}
        </nav>
        <div className="mt-auto rounded-xl bg-white/5 p-3">
          <div className="text-sm font-semibold">{username}</div>
          <div className="mt-0.5 flex flex-wrap gap-1">
            {roles.map((r) => (
              <span key={r} className="rounded-full bg-white/10 px-2 py-0.5 text-[10px] uppercase tracking-wide text-white/70">
                {r.replace('ROLE_', '')}
              </span>
            ))}
          </div>
          <button
            onClick={() => { dispatch(loggedOut()); navigate('/login') }}
            className="mt-3 w-full rounded-lg border border-white/20 py-1.5 text-xs font-semibold text-white/80 hover:bg-white/10"
          >
            Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 bg-paper">
        <div className="mx-auto max-w-6xl px-8 py-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
