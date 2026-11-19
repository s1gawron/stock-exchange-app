import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/api'
import { day, money, pct, signColor } from '../lib/format'
import type { Leaderboard, LeaderboardEntry } from '../lib/types'
import { Card, ErrorBox } from '../components/ui'

function Row({ e }: { e: LeaderboardEntry }) {
  return (
    <tr className={`border-b border-slate-100 last:border-0 ${e.me ? 'bg-sky-50 font-medium' : ''}`}>
      <td className="px-4 py-2 text-right">{e.rank}</td>
      <td className="px-4 py-2">{e.displayName}{e.me && <span className="ml-2 text-xs text-sky-700">you</span>}</td>
      <td className={`px-4 py-2 text-right ${signColor(e.returnPercent)}`}>{pct(e.returnPercent)}</td>
      <td className="px-4 py-2 text-right">{money(e.equity)}</td>
    </tr>
  )
}

export function LeaderboardPage() {
  const board = useQuery({ queryKey: ['leaderboard'], queryFn: () => api<Leaderboard>('/api/leaderboard?limit=50') })

  if (board.isPending) return <p className="text-slate-500">Loading…</p>
  if (board.isError) return <ErrorBox error={board.error} />
  const b = board.data
  const meInTop = b.top.some((e) => e.me)

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Leaderboard</h1>
        <p className="mt-1 text-sm text-slate-600">
          {b.asOf ? `Return since account opening · as of the close on ${day(b.asOf)} · ${b.players} players`
            : 'The first ranking appears after the next US close.'}
        </p>
      </div>
      {b.asOf && (
        <Card className="overflow-x-auto p-0">
          <table className="w-full text-sm">
            <thead className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="w-16 px-4 py-2 text-right">#</th>
                <th className="px-4 py-2">Player</th>
                <th className="px-4 py-2 text-right">Return</th>
                <th className="px-4 py-2 text-right">Value</th>
              </tr>
            </thead>
            <tbody className="tabular-nums">
              {b.top.map((e) => <Row key={`${e.rank}-${e.displayName}`} e={e} />)}
              {!meInTop && b.me && (
                <>
                  <tr><td colSpan={4} className="px-4 py-1 text-center text-slate-400">⋯</td></tr>
                  <Row e={b.me} />
                </>
              )}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}
