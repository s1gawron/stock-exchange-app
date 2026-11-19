import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useRef, useState, type FormEvent } from 'react'
import { useParams } from 'react-router'
import { api } from '../lib/api'
import { dateTime, money, pct, qty as fmtQty, rejectText, signColor } from '../lib/format'
import type { Instrument, Order, Portfolio, Quote, Side } from '../lib/types'
import { buttonClass, Card, ErrorBox, inputClass } from '../components/ui'

export function StockPage() {
  const { symbol = '' } = useParams()
  const qc = useQueryClient()
  const instrument = useQuery({
    queryKey: ['instrument', symbol],
    queryFn: () => api<Instrument>(`/api/instruments/${encodeURIComponent(symbol)}`),
  })
  const quote = useQuery({
    queryKey: ['quote', symbol],
    queryFn: () => api<Quote>(`/api/quotes/${encodeURIComponent(symbol)}`),
    refetchInterval: 15_000,
  })
  const portfolio = useQuery({ queryKey: ['portfolio'], queryFn: () => api<Portfolio>('/api/portfolio') })
  const held = portfolio.data?.holdings.find((h) => h.symbol === symbol)?.qty ?? 0

  const [side, setSide] = useState<Side>('BUY')
  const [qty, setQty] = useState('1')
  // One key per order attempt: a retry of the same attempt (e.g. double click) reuses it.
  const idempotencyKey = useRef(crypto.randomUUID())

  const place = useMutation({
    mutationFn: () =>
      api<Order>('/api/orders', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey.current },
        body: { symbol, side, qty: Number(qty) },
      }),
    onSuccess: () => {
      idempotencyKey.current = crypto.randomUUID()
      qc.invalidateQueries({ queryKey: ['portfolio'] })
      qc.invalidateQueries({ queryKey: ['orders'] })
    },
  })

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    place.mutate()
  }

  if (instrument.isError) return <ErrorBox error={instrument.error} />
  const estimate = quote.data ? quote.data.price * Number(qty || 0) : null
  const result = place.data

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        {instrument.data?.logoUrl && <img src={instrument.data.logoUrl} alt="" className="h-10 w-10 rounded" />}
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{symbol}</h1>
          <p className="text-sm text-slate-600">
            {instrument.data?.name}
            {instrument.data?.exchange && ` · ${instrument.data.exchange}`}
          </p>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <ErrorBox error={quote.error} />
          {quote.data && (
            <>
              <div className="text-3xl font-semibold tabular-nums">{money(quote.data.price)}</div>
              <div className={`tabular-nums ${signColor(quote.data.change)}`}>
                {money(quote.data.change)} ({pct(quote.data.changePercent)})
              </div>
              <dl className="mt-4 grid grid-cols-2 gap-y-1 text-sm tabular-nums">
                <dt className="text-slate-500">Open</dt><dd>{money(quote.data.open)}</dd>
                <dt className="text-slate-500">High</dt><dd>{money(quote.data.high)}</dd>
                <dt className="text-slate-500">Low</dt><dd>{money(quote.data.low)}</dd>
                <dt className="text-slate-500">Prev. close</dt><dd>{money(quote.data.previousClose)}</dd>
                <dt className="text-slate-500">Last trade</dt><dd>{dateTime(quote.data.timestamp)}</dd>
              </dl>
            </>
          )}
        </Card>

        <Card>
          <form onSubmit={onSubmit} className="space-y-3">
            <div className="grid grid-cols-2 gap-1 rounded-md bg-slate-100 p-1" role="group" aria-label="Side">
              {(['BUY', 'SELL'] as const).map((s) => (
                <button key={s} type="button" onClick={() => setSide(s)} aria-pressed={side === s}
                  className={`rounded px-3 py-1.5 text-sm font-medium ${side === s ? 'bg-white shadow-sm' : 'text-slate-600'}`}>
                  {s === 'BUY' ? 'Buy' : 'Sell'}
                </button>
              ))}
            </div>
            <label className="block text-sm">
              Shares
              <input className={inputClass} type="number" min={1} step={1} required value={qty}
                onChange={(e) => setQty(e.target.value)} />
            </label>
            <div className="text-sm text-slate-600 tabular-nums">
              <div>Estimated {side === 'BUY' ? 'cost' : 'proceeds'}: {money(estimate)}</div>
              <div>Cash: {money(portfolio.data?.cash)} · You hold: {fmtQty(held)}</div>
            </div>
            <ErrorBox error={place.error} />
            <button type="submit" className={`${buttonClass} w-full`} disabled={place.isPending}>
              {place.isPending ? 'Placing…' : `${side === 'BUY' ? 'Buy' : 'Sell'} at market`}
            </button>
            {result && (
              <p role="status" className={`text-sm ${result.status === 'FILLED' ? 'text-emerald-700' : 'text-rose-700'}`}>
                {result.status === 'FILLED' && result.fill
                  ? `Filled: ${result.side === 'BUY' ? 'bought' : 'sold'} ${fmtQty(result.qty)} at ${money(result.fill.price)}.`
                  : `Rejected: ${rejectText[result.rejectReason ?? ''] ?? result.rejectReason}.`}
              </p>
            )}
          </form>
        </Card>
      </div>
    </div>
  )
}
