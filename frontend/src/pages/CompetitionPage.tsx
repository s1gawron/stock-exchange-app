import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { api } from '../lib/api'
import { day, pct, signColor, statusBadge } from '../lib/format'
import type { CompetitionDetails } from '../lib/types'
import { Card, ErrorBox } from '../components/ui'

export function CompetitionPage() {
  const { id = '' } = useParams()
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [copied, setCopied] = useState(false)
  const details = useQuery({
    queryKey: ['competition', id],
    queryFn: () => api<CompetitionDetails>(`/api/competitions/${encodeURIComponent(id)}`),
  })
  const leave = useMutation({
    mutationFn: () => api<void>(`/api/competitions/${encodeURIComponent(id)}/leave`, { method: 'POST' }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['competitions'] })
      navigate('/competitions')
    },
  })

  if (details.isPending) return <p className="text-slate-500">Loading…</p>
  if (details.isError) return <ErrorBox error={details.error} />
  const { competition: c, asOf, standings } = details.data

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(c.code)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      // clipboard blocked: the code is visible anyway
    }
  }

  return (
    <div className="space-y-6">
      <Link to="/competitions" className="text-sm text-slate-600 hover:underline">← Competitions</Link>
      <div className="flex flex-wrap items-center gap-3">
        <h1 className="text-2xl font-semibold tracking-tight">{c.name}</h1>
        <span className={`rounded px-2 py-0.5 text-xs ${statusBadge[c.status]}`}>{c.status.toLowerCase()}</span>
      </div>
      <p className="text-sm text-slate-600">
        {day(c.startsOn)} – {day(c.endsOn)} · {c.members} players · Score = % return of each normal account,
        from the close before the start to the close on the end day.
      </p>

      {c.status === 'UPCOMING' && (
        <Card className="flex flex-wrap items-center gap-3">
          <span className="text-sm text-slate-600">Join code</span>
          <code className="rounded bg-slate-100 px-3 py-1 text-lg tracking-widest">{c.code}</code>
          <button type="button" onClick={copy} className="rounded-md border border-slate-300 px-3 py-1 text-sm hover:bg-slate-100">
            {copied ? 'Copied' : 'Copy'}
          </button>
          <span className="text-xs text-slate-500">Players can join until the US open on {day(c.startsOn)}.</span>
          <button type="button" onClick={() => leave.mutate()} disabled={leave.isPending}
            className="ml-auto rounded-md border border-rose-300 px-3 py-1 text-sm text-rose-700 hover:bg-rose-50">
            {c.creator ? 'Delete competition' : 'Leave'}
          </button>
        </Card>
      )}
      <ErrorBox error={leave.error} />

      <Card className="overflow-x-auto p-0">
        <div className="border-b border-slate-200 px-4 py-2 text-xs text-slate-500">
          {asOf ? `Standings as of the close on ${day(asOf)}` : 'Scores appear after the first close inside the competition.'}
        </div>
        <table className="w-full text-sm">
          <thead className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="w-16 px-4 py-2 text-right">#</th>
              <th className="px-4 py-2">Player</th>
              <th className="px-4 py-2 text-right">Return</th>
            </tr>
          </thead>
          <tbody className="tabular-nums">
            {standings.map((s) => (
              <tr key={`${s.rank}-${s.displayName}`} className={`border-b border-slate-100 last:border-0 ${s.me ? 'bg-sky-50 font-medium' : ''}`}>
                <td className="px-4 py-2 text-right">{s.returnPercent == null ? '—' : s.rank}</td>
                <td className="px-4 py-2">{s.displayName}{s.me && <span className="ml-2 text-xs text-sky-700">you</span>}</td>
                <td className={`px-4 py-2 text-right ${signColor(s.returnPercent)}`}>{pct(s.returnPercent)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
