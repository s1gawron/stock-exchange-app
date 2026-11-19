import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useRef, useState, type FormEvent } from 'react'
import { useParams } from 'react-router'
import { api } from '../lib/api'
import { dateTime, money, orderTypeText, pct, qty as fmtQty, rejectText, sessionLabel, signColor } from '../lib/format'
import { useLivePrices } from '../lib/live'
import type {
  Instrument, Order, OrderType, Portfolio, Quote, Side, TimeInForce, WatchlistItem,
} from '../lib/types'
import { CancelButton } from '../components/CancelButton'
import { buttonClass, Card, ErrorBox, inputClass } from '../components/ui'

function Segmented<T extends string>({ value, options, onChange, label }: {
  value: T
  options: { value: T; label: string }[]
  onChange: (v: T) => void
  label: string
}) {
  return (
    <div className="grid gap-1 rounded-md bg-slate-100 p-1" style={{ gridTemplateColumns: `repeat(${options.length}, 1fr)` }}
      role="group" aria-label={label}>
      {options.map((o) => (
        <button key={o.value} type="button" onClick={() => onChange(o.value)} aria-pressed={value === o.value}
          className={`rounded px-3 py-1.5 text-sm font-medium ${value === o.value ? 'bg-white shadow-sm' : 'text-slate-600'}`}>
          {o.label}
        </button>
      ))}
    </div>
  )
}

function WatchToggle({ symbol }: { symbol: string }) {
  const qc = useQueryClient()
  const list = useQuery({ queryKey: ['watchlist'], queryFn: () => api<WatchlistItem[]>('/api/watchlist') })
  const watched = list.data?.some((i) => i.symbol === symbol) ?? false
  const toggle = useMutation({
    mutationFn: () => api<void>(`/api/watchlist/${symbol}`, { method: watched ? 'DELETE' : 'PUT' }),
    onSettled: () => qc.invalidateQueries({ queryKey: ['watchlist'] }),
  })
  return (
    <button type="button" onClick={() => toggle.mutate()} disabled={list.isPending || toggle.isPending}
      aria-pressed={watched} title={toggle.error instanceof Error ? toggle.error.message : undefined}
      className="ml-auto rounded-md border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-100">
      {watched ? '★ Watching' : '☆ Watch'}
    </button>
  )
}

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
    refetchInterval: 60_000,
  })
  const live = useLivePrices([symbol])[symbol]
  const portfolio = useQuery({ queryKey: ['portfolio'], queryFn: () => api<Portfolio>('/api/portfolio') })
  const openOrders = useQuery({
    queryKey: ['orders', 'OPEN'],
    queryFn: () => api<Order[]>('/api/orders?status=OPEN&size=100'),
  })
  const holding = portfolio.data?.holdings.find((h) => h.symbol === symbol)
  const freeShares = holding ? holding.qty - holding.reservedQty : 0

  const [side, setSide] = useState<Side>('BUY')
  const [type, setType] = useState<OrderType>('MARKET')
  const [tif, setTif] = useState<TimeInForce>('DAY')
  const [qty, setQty] = useState('1')
  const [price, setPrice] = useState('')
  // One key per order attempt: a retry of the same attempt (e.g. double click) reuses it.
  const idempotencyKey = useRef(crypto.randomUUID())

  const place = useMutation({
    mutationFn: () =>
      api<Order>('/api/orders', {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey.current },
        body: {
          symbol, side, type, qty: Number(qty),
          limitPrice: type === 'LIMIT' ? Number(price) : undefined,
          stopPrice: type === 'STOP' ? Number(price) : undefined,
          timeInForce: type === 'MARKET' ? undefined : tif,
        },
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
  const q = quote.data
  const lastPrice = live?.price ?? q?.price
  const extendedHours = sessionLabel(live?.session ?? q?.session)
  const change = lastPrice != null && q ? lastPrice - q.previousClose : null
  const changePct = change != null && q?.previousClose ? (change / q.previousClose) * 100 : null
  const unitPrice = type === 'MARKET' ? lastPrice : Number(price) || null
  const estimate = unitPrice != null ? unitPrice * Number(qty || 0) : null
  const result = place.data
  const symbolOrders = openOrders.data?.filter((o) => o.symbol === symbol) ?? []

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
        <WatchToggle symbol={symbol} />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <ErrorBox error={quote.error} />
          {q && (
            <>
              <div className="flex items-baseline gap-2">
                <span className="text-3xl font-semibold tabular-nums">{money(lastPrice)}</span>
                {extendedHours && (
                  <span className="rounded bg-amber-100 px-1.5 py-0.5 text-xs text-amber-800"
                    title="Trade outside regular US hours (09:30–16:00 New York). Orders fill only in regular hours.">
                    {extendedHours}
                  </span>
                )}
                {live && <span className="text-xs text-emerald-600">live</span>}
              </div>
              <div className={`tabular-nums ${signColor(change)}`}>{money(change)} ({pct(changePct)})</div>
              <dl className="mt-4 grid grid-cols-2 gap-y-1 text-sm tabular-nums">
                <dt className="text-slate-500">Open</dt><dd>{money(q.open)}</dd>
                <dt className="text-slate-500">High</dt><dd>{money(Math.max(q.high, lastPrice ?? q.high))}</dd>
                <dt className="text-slate-500">Low</dt><dd>{money(Math.min(q.low, lastPrice ?? q.low))}</dd>
                <dt className="text-slate-500">Prev. close</dt><dd>{money(q.previousClose)}</dd>
                <dt className="text-slate-500">Last trade</dt>
                <dd>
                  {dateTime(live?.timestamp ?? q.timestamp)}
                  {extendedHours && <span className="ml-1 text-xs text-amber-700">({extendedHours.toLowerCase()})</span>}
                </dd>
              </dl>
            </>
          )}
        </Card>

        <Card>
          <form onSubmit={onSubmit} className="space-y-3">
            <Segmented label="Side" value={side} onChange={setSide}
              options={[{ value: 'BUY', label: 'Buy' }, { value: 'SELL', label: 'Sell' }]} />
            <Segmented label="Order type" value={type} onChange={setType}
              options={[{ value: 'MARKET', label: 'Market' }, { value: 'LIMIT', label: 'Limit' }, { value: 'STOP', label: 'Stop' }]} />
            <div className="grid grid-cols-2 gap-3">
              <label className="block text-sm">
                Shares
                <input className={inputClass} type="number" min={1} step={1} required value={qty}
                  onChange={(e) => setQty(e.target.value)} />
              </label>
              {type !== 'MARKET' && (
                <label className="block text-sm">
                  {type === 'LIMIT' ? 'Limit price' : 'Stop price'}
                  <input className={inputClass} type="number" min={0.0001} step={0.0001} required value={price}
                    onChange={(e) => setPrice(e.target.value)} placeholder={lastPrice?.toFixed(2)} />
                </label>
              )}
            </div>
            {type !== 'MARKET' && (
              <Segmented label="Time in force" value={tif} onChange={setTif}
                options={[{ value: 'DAY', label: 'Day' }, { value: 'GTC', label: 'Until cancelled' }]} />
            )}
            <p className="text-xs text-slate-500">
              {type === 'MARKET' && 'Fills now at the last price. Only during US market hours.'}
              {type === 'LIMIT' && (side === 'BUY' ? 'Buys when the price is at or below your limit.' : 'Sells when the price is at or above your limit.')}
              {type === 'STOP' && (side === 'BUY' ? 'Buys at market when the price rises to your stop.' : 'Sells at market when the price falls to your stop.')}
              {type !== 'MARKET' && ' Can be placed any time; fills only during market hours.'}
            </p>
            <div className="text-sm text-slate-600 tabular-nums">
              <div>Estimated {side === 'BUY' ? 'cost' : 'proceeds'}: {money(estimate)}</div>
              <div>Cash available: {money(portfolio.data?.availableCash)} · Free shares: {fmtQty(freeShares)}</div>
            </div>
            <ErrorBox error={place.error} />
            <button type="submit" className={`${buttonClass} w-full`} disabled={place.isPending}>
              {place.isPending ? 'Placing…' : `${side === 'BUY' ? 'Buy' : 'Sell'} ${type === 'MARKET' ? 'at market' : type.toLowerCase()}`}
            </button>
            {result && (
              <p role="status" className={`text-sm ${result.status === 'REJECTED' ? 'text-rose-700' : 'text-emerald-700'}`}>
                {result.status === 'FILLED' && result.fill &&
                  `Filled: ${result.side === 'BUY' ? 'bought' : 'sold'} ${fmtQty(result.qty)} at ${money(result.fill.price)}.`}
                {result.status === 'OPEN' && `Order placed. It waits until the price reaches ${money(result.limitPrice ?? result.stopPrice)}.`}
                {result.status === 'REJECTED' && `Rejected: ${rejectText[result.rejectReason ?? ''] ?? result.rejectReason}.`}
              </p>
            )}
          </form>
        </Card>
      </div>

      {symbolOrders.length > 0 && (
        <section className="space-y-2">
          <h2 className="text-lg font-semibold">Open orders</h2>
          <Card className="p-0">
            <ul>
              {symbolOrders.map((o) => (
                <li key={o.id} className="flex flex-wrap items-center gap-3 border-b border-slate-100 px-4 py-2.5 text-sm last:border-0">
                  <span className="font-medium">{o.side === 'BUY' ? 'Buy' : 'Sell'} {fmtQty(o.qty)}</span>
                  <span>{orderTypeText(o)}</span>
                  <span className="text-slate-500">{o.timeInForce === 'DAY' ? `Day (until ${dateTime(o.expiresAt!)})` : 'Until cancelled'}</span>
                  <span className="ml-auto"><CancelButton order={o} /></span>
                </li>
              ))}
            </ul>
          </Card>
        </section>
      )}
    </div>
  )
}
