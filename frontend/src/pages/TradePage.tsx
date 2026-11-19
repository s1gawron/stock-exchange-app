import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router'
import { api } from '../lib/api'
import { money, pct, signColor } from '../lib/format'
import { useLivePrices } from '../lib/live'
import type { Instrument, WatchlistItem } from '../lib/types'
import { Card, ErrorBox, inputClass } from '../components/ui'

function Watchlist() {
  const qc = useQueryClient()
  const list = useQuery({ queryKey: ['watchlist'], queryFn: () => api<WatchlistItem[]>('/api/watchlist') })
  const live = useLivePrices(list.data?.map((i) => i.symbol) ?? [])
  const remove = useMutation({
    mutationFn: (symbol: string) => api<void>(`/api/watchlist/${symbol}`, { method: 'DELETE' }),
    onSettled: () => qc.invalidateQueries({ queryKey: ['watchlist'] }),
  })

  return (
    <section className="space-y-2">
      <h2 className="text-lg font-semibold">Watchlist</h2>
      <ErrorBox error={list.error ?? remove.error} />
      <Card className="p-0">
        {list.data?.length === 0 ? (
          <p className="p-4 text-sm text-slate-600">Empty. Open a stock and click ☆ to watch it.</p>
        ) : (
          <ul>
            {list.data?.map((i) => {
              const price = live[i.symbol]?.price ?? i.price
              const prevClose = i.price != null && i.change != null ? i.price - i.change : null
              const change = price != null && prevClose != null ? price - prevClose : i.change
              const changePct = change != null && prevClose ? (change / prevClose) * 100 : i.changePercent
              return (
                <li key={i.symbol} className="flex items-center gap-3 border-b border-slate-100 px-4 py-2.5 last:border-0">
                  <Link to={`/stocks/${i.symbol}`} className="w-20 font-medium hover:underline">{i.symbol}</Link>
                  <span className="flex-1 truncate text-sm text-slate-600">{i.name}</span>
                  <span className="tabular-nums">{money(price)}</span>
                  <span className={`w-20 text-right text-sm tabular-nums ${signColor(change)}`}>{pct(changePct)}</span>
                  <button type="button" aria-label={`Remove ${i.symbol} from watchlist`}
                    onClick={() => remove.mutate(i.symbol)} className="text-slate-400 hover:text-slate-900">✕</button>
                </li>
              )
            })}
          </ul>
        )}
      </Card>
    </section>
  )
}

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
    <div className="space-y-6">
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
      <Watchlist />
    </div>
  )
}
