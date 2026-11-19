import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { api } from '../lib/api'
import { day, statusBadge } from '../lib/format'
import type { Competition } from '../lib/types'
import { buttonClass, Card, ErrorBox, inputClass } from '../components/ui'

const DAY_MS = 86_400_000
const isoDate = (ms: number) => new Date(ms).toISOString().slice(0, 10)
/** Default: start tomorrow, run four weeks. */
const defaultDates = () => {
  const tomorrow = Date.now() + DAY_MS
  return { start: isoDate(tomorrow), end: isoDate(tomorrow + 27 * DAY_MS) }
}

export function CompetitionsPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const list = useQuery({ queryKey: ['competitions'], queryFn: () => api<Competition[]>('/api/competitions') })

  const [name, setName] = useState('')
  const [startsOn, setStartsOn] = useState(() => defaultDates().start)
  const [endsOn, setEndsOn] = useState(() => defaultDates().end)
  const [code, setCode] = useState('')

  const create = useMutation({
    mutationFn: () => api<Competition>('/api/competitions', { method: 'POST', body: { name, startsOn, endsOn } }),
    onSuccess: (c) => {
      qc.invalidateQueries({ queryKey: ['competitions'] })
      navigate(`/competitions/${c.id}`)
    },
  })
  const join = useMutation({
    mutationFn: () => api<Competition>('/api/competitions/join', { method: 'POST', body: { code } }),
    onSuccess: (c) => {
      qc.invalidateQueries({ queryKey: ['competitions'] })
      navigate(`/competitions/${c.id}`)
    },
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Competitions</h1>
        <p className="mt-1 text-sm text-slate-600">
          Compete with friends on % return of your normal account between two dates. Join with a code before the start.
        </p>
      </div>

      <Card className="p-0">
        <ErrorBox error={list.error} />
        {list.data?.length === 0 ? (
          <p className="p-4 text-sm text-slate-600">You are not in any competition yet.</p>
        ) : (
          <ul>
            {list.data?.map((c) => (
              <li key={c.id} className="border-b border-slate-100 last:border-0">
                <Link to={`/competitions/${c.id}`} className="flex flex-wrap items-center gap-3 px-4 py-3 hover:bg-slate-50">
                  <span className="font-medium">{c.name}</span>
                  <span className={`rounded px-2 py-0.5 text-xs ${statusBadge[c.status]}`}>{c.status.toLowerCase()}</span>
                  <span className="ml-auto text-sm text-slate-600">{day(c.startsOn)} – {day(c.endsOn)} · {c.members} players</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <h2 className="font-semibold">Join with a code</h2>
          <form className="mt-3 space-y-3" onSubmit={(e: FormEvent) => { e.preventDefault(); join.mutate() }}>
            <input className={`${inputClass} uppercase tracking-widest`} placeholder="ABCD2345" maxLength={16} required
              value={code} onChange={(e) => setCode(e.target.value)} aria-label="Competition code" />
            <ErrorBox error={join.error} />
            <button type="submit" className={buttonClass} disabled={join.isPending}>Join</button>
          </form>
        </Card>
        <Card>
          <h2 className="font-semibold">Create a competition</h2>
          <form className="mt-3 space-y-3" onSubmit={(e: FormEvent) => { e.preventDefault(); create.mutate() }}>
            <input className={inputClass} placeholder="Name" maxLength={100} required value={name}
              onChange={(e) => setName(e.target.value)} aria-label="Competition name" />
            <div className="grid grid-cols-2 gap-3">
              <label className="block text-sm">Start
                <input className={inputClass} type="date" required value={startsOn} onChange={(e) => setStartsOn(e.target.value)} />
              </label>
              <label className="block text-sm">End
                <input className={inputClass} type="date" required value={endsOn} min={startsOn} onChange={(e) => setEndsOn(e.target.value)} />
              </label>
            </div>
            <ErrorBox error={create.error} />
            <button type="submit" className={buttonClass} disabled={create.isPending}>Create</button>
          </form>
        </Card>
      </div>
    </div>
  )
}
