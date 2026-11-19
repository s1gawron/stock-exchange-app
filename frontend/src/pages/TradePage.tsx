import { useQuery } from '@tanstack/react-query'
import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router'
import { api } from '../lib/api'
import type { Instrument } from '../lib/types'
import { Card, ErrorBox, inputClass } from '../components/ui'

export function TradePage() {
  const [q, setQ] = useState('')
  const query = useDeferredValue(q.trim())
  const results = useQuery({
    queryKey: ['search', query],
    queryFn: () => api<Instrument[]>(`/api/instruments?q=${encodeURIComponent(query)}`),
    enabled: query.length > 0,
    staleTime: 60_000,
  })

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold tracking-tight">Trade</h1>
      <input className={inputClass} placeholder="Search by symbol or company name, e.g. AAPL or Apple"
        value={q} onChange={(e) => setQ(e.target.value)} autoFocus aria-label="Search stocks" />
      <ErrorBox error={results.error} />
      {results.data && (
        <Card className="p-0">
          {results.data.length === 0 ? (
            <p className="p-4 text-sm text-slate-600">No matches.</p>
          ) : (
            <ul>
              {results.data.map((i) => (
                <li key={i.symbol} className="border-b border-slate-100 last:border-0">
                  <Link to={`/stocks/${i.symbol}`} className="flex items-center gap-3 px-4 py-2.5 hover:bg-slate-50">
                    <span className="w-20 font-medium">{i.displaySymbol}</span>
                    <span className="flex-1 truncate text-sm text-slate-600">{i.name}</span>
                    <span className="text-xs text-slate-400">{i.type}</span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}
    </div>
  )
}
