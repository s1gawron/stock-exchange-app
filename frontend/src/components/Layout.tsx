import { useQuery } from '@tanstack/react-query'
import { NavLink, Outlet } from 'react-router'
import { api } from '../lib/api'
import { useLogout, useMe } from '../lib/auth'
import { useLiveStatus } from '../lib/live'
import type { MarketStatus } from '../lib/types'
import { OrderToast } from './OrderToast'
import { ThemeToggle } from './ThemeToggle'

const navClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-1.5 text-sm font-medium ${
    isActive ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'
  }`

export function Layout() {
  const me = useMe()
  const logout = useLogout()
  const { connected } = useLiveStatus()
  const market = useQuery({
    queryKey: ['market-status'],
    queryFn: () => api<MarketStatus>('/api/market/status'),
    refetchInterval: 60_000,
  })

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-3 px-4 py-3">
          <span className="mr-2 font-semibold tracking-tight">Stock Simulator</span>
          <nav className="flex flex-wrap gap-1">
            <NavLink to="/" end className={navClass}>
              Portfolio
            </NavLink>
            <NavLink to="/trade" className={navClass}>
              Trade
            </NavLink>
            <NavLink to="/orders" className={navClass}>
              Orders
            </NavLink>
            <NavLink to="/performance" className={navClass}>
              Performance
            </NavLink>
            <NavLink to="/leaderboard" className={navClass}>
              Leaderboard
            </NavLink>
            <NavLink to="/competitions" className={navClass}>
              Competitions
            </NavLink>
          </nav>
          <div className="ml-auto flex items-center gap-3 text-sm">
            {market.data && (
              <span className="flex items-center gap-1.5 text-slate-600" title="US market (NYSE/Nasdaq)">
                <span
                  className={`h-2 w-2 rounded-full ${market.data.open ? 'bg-emerald-500' : 'bg-slate-400'}`}
                  aria-hidden="true"
                />
                {market.data.description}
              </span>
            )}
            <span className={`text-xs ${connected ? 'text-emerald-600' : 'text-slate-400'}`}
              title={connected ? 'Live prices connected' : 'Live prices reconnecting'}>
              {connected ? '● Live' : '○ Offline'}
            </span>
            <span className="hidden text-slate-500 sm:inline">{me.data?.displayName}</span>
            <ThemeToggle />
            <button
              type="button"
              onClick={() => logout.mutate()}
              className="rounded-md border border-slate-300 px-2.5 py-1 hover:bg-slate-100"
            >
              Log out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Outlet />
      </main>
      <OrderToast />
    </div>
  )
}
