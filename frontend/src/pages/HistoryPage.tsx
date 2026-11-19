import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router'
import { api } from '../lib/api'
import { dateTime, money, orderTypeText, qty, rejectText, signColor } from '../lib/format'
import type { Order } from '../lib/types'
import { CancelButton } from '../components/CancelButton'
import { Card, ErrorBox } from '../components/ui'

const PAGE_SIZE = 20

function StatusCell({ o }: { o: Order }) {
  switch (o.status) {
    case 'FILLED':
      return <span className="text-emerald-700">Filled</span>
    case 'OPEN':
      return (
        <span className="flex items-center gap-2">
          <span className="text-sky-700">Open{o.expiresAt ? ' (day)' : ''}</span>
          <CancelButton order={o} />
        </span>
      )
    case 'REJECTED':
      return <span className="text-rose-700">Rejected: {rejectText[o.rejectReason ?? ''] ?? o.rejectReason}</span>
    case 'CANCELLED':
      return <span className="text-slate-500">Cancelled</span>
    case 'EXPIRED':
      return <span className="text-slate-500">Expired</span>
  }
}

export function HistoryPage() {
  const [filter, setFilter] = useState<'ALL' | 'OPEN'>('ALL')
  const [page, setPage] = useState(0)
  const orders = useQuery({
    queryKey: ['orders', filter, page],
    queryFn: () =>
      api<Order[]>(`/api/orders?page=${page}&size=${PAGE_SIZE}${filter === 'OPEN' ? '&status=OPEN' : ''}`),
  })

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-3">
        <h1 className="text-2xl font-semibold tracking-tight">Orders</h1>
        <div className="flex gap-1 rounded-md bg-slate-100 p-1 text-sm" role="group" aria-label="Filter">
          {(['ALL', 'OPEN'] as const).map((f) => (
            <button key={f} type="button" aria-pressed={filter === f}
              onClick={() => { setFilter(f); setPage(0) }}
              className={`rounded px-3 py-1 ${filter === f ? 'bg-white shadow-sm' : 'text-slate-600'}`}>
              {f === 'ALL' ? 'All' : 'Open'}
            </button>
          ))}
        </div>
      </div>
      <ErrorBox error={orders.error} />
      <Card className="overflow-x-auto p-0">
        {orders.data?.length === 0 ? (
          <p className="p-4 text-sm text-slate-600">No orders.</p>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-2">Time</th>
                <th className="px-4 py-2">Symbol</th>
                <th className="px-4 py-2">Side</th>
                <th className="px-4 py-2">Type</th>
                <th className="px-4 py-2 text-right">Qty</th>
                <th className="px-4 py-2 text-right">Fill price</th>
                <th className="px-4 py-2 text-right">Amount</th>
                <th className="px-4 py-2 text-right">Realized P&L</th>
                <th className="px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody className="tabular-nums">
              {orders.data?.map((o) => (
                <tr key={o.id} className="border-b border-slate-100 last:border-0">
                  <td className="whitespace-nowrap px-4 py-2 text-slate-600">{dateTime(o.createdAt)}</td>
                  <td className="px-4 py-2"><Link to={`/stocks/${o.symbol}`} className="font-medium hover:underline">{o.symbol}</Link></td>
                  <td className="px-4 py-2">{o.side}</td>
                  <td className="whitespace-nowrap px-4 py-2">{orderTypeText(o)}</td>
                  <td className="px-4 py-2 text-right">{qty(o.qty)}</td>
                  <td className="px-4 py-2 text-right">{money(o.fill?.price)}</td>
                  <td className="px-4 py-2 text-right">{money(o.fill?.grossAmount)}</td>
                  <td className={`px-4 py-2 text-right ${signColor(o.fill?.realizedPnl)}`}>
                    {o.fill?.realizedPnl == null ? '' : money(o.fill.realizedPnl)}
                  </td>
                  <td className="whitespace-nowrap px-4 py-2"><StatusCell o={o} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
      <div className="flex gap-2">
        <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm disabled:opacity-40"
          disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
        <button type="button" className="rounded-md border border-slate-300 px-3 py-1 text-sm disabled:opacity-40"
          disabled={(orders.data?.length ?? 0) < PAGE_SIZE} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </div>
  )
}
