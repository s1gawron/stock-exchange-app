import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { api } from '../lib/api'
import { money, pct, qty, signColor } from '../lib/format'
import type { Portfolio } from '../lib/types'
import { Card, ErrorBox, Stat } from '../components/ui'

export function PortfolioPage() {
  const portfolio = useQuery({
    queryKey: ['portfolio'],
    queryFn: () => api<Portfolio>('/api/portfolio'),
    refetchInterval: 30_000,
  })

  if (portfolio.isPending) return <p className="text-slate-500">Loading…</p>
  if (portfolio.isError) return <ErrorBox error={portfolio.error} />
  const p = portfolio.data

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Portfolio</h1>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        <Stat label="Total value" value={money(p.equity)} />
        <Stat label="Cash" value={money(p.cash)} />
        <Stat label="Total return" value={`${money(p.totalReturn)} (${pct(p.totalReturnPercent)})`}
          className={signColor(p.totalReturn)} />
        <Stat label="Realized P&L" value={money(p.realizedPnl)} className={signColor(p.realizedPnl)} />
      </div>
      {p.pricesIncomplete && (
        <p className="text-sm text-amber-700">Some prices are unavailable right now. Totals leave those holdings out.</p>
      )}

      <Card className="overflow-x-auto p-0">
        {p.holdings.length === 0 ? (
          <p className="p-4 text-sm text-slate-600">
            No holdings yet. <Link to="/trade" className="underline">Find a stock to buy</Link>.
          </p>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-2">Symbol</th>
                <th className="px-4 py-2 text-right">Qty</th>
                <th className="px-4 py-2 text-right">Avg cost</th>
                <th className="px-4 py-2 text-right">Price</th>
                <th className="px-4 py-2 text-right">Value</th>
                <th className="px-4 py-2 text-right">Unrealized P&L</th>
              </tr>
            </thead>
            <tbody className="tabular-nums">
              {p.holdings.map((h) => (
                <tr key={h.symbol} className="border-b border-slate-100 last:border-0">
                  <td className="px-4 py-2">
                    <Link to={`/stocks/${h.symbol}`} className="font-medium hover:underline">{h.symbol}</Link>
                    <div className="max-w-48 truncate text-xs text-slate-500">{h.name}</div>
                  </td>
                  <td className="px-4 py-2 text-right">{qty(h.qty)}</td>
                  <td className="px-4 py-2 text-right">{money(h.avgCost)}</td>
                  <td className="px-4 py-2 text-right">{money(h.lastPrice)}</td>
                  <td className="px-4 py-2 text-right">{money(h.marketValue)}</td>
                  <td className={`px-4 py-2 text-right ${signColor(h.unrealizedPnl)}`}>
                    {money(h.unrealizedPnl)} <span className="text-xs">{pct(h.unrealizedPnlPercent)}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
