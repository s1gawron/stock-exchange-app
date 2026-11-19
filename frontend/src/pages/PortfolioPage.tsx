import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { api } from '../lib/api'
import { money, pct, qty, signColor } from '../lib/format'
import { useLivePrices } from '../lib/live'
import type { Portfolio } from '../lib/types'
import { Card, ErrorBox, Stat } from '../components/ui'

export function PortfolioPage() {
  const portfolio = useQuery({
    queryKey: ['portfolio'],
    queryFn: () => api<Portfolio>('/api/portfolio'),
    refetchInterval: 60_000,
  })
  const live = useLivePrices(portfolio.data?.holdings.map((h) => h.symbol) ?? [])

  if (portfolio.isPending) return <p className="text-slate-500">Loading…</p>
  if (portfolio.isError) return <ErrorBox error={portfolio.error} />
  const p = portfolio.data

  // Live prices update values between server refreshes.
  const holdings = p.holdings.map((h) => {
    const price = live[h.symbol]?.price ?? h.lastPrice
    if (price == null) return { ...h, lastPrice: null }
    const marketValue = Math.round(price * h.qty * 100) / 100
    const unrealizedPnl = marketValue - h.costBasis
    return {
      ...h,
      lastPrice: price,
      marketValue,
      unrealizedPnl,
      unrealizedPnlPercent: h.costBasis ? (unrealizedPnl / h.costBasis) * 100 : 0,
    }
  })
  const marketValue = holdings.reduce((sum, h) => sum + (h.marketValue ?? 0), 0)
  const equity = p.cash + marketValue
  const totalReturn = equity - p.deposits
  const totalReturnPct = p.deposits ? (totalReturn / p.deposits) * 100 : 0

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Portfolio</h1>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        <Stat label="Total value" value={money(equity)} />
        <Stat label="Cash available"
          value={<>
            {money(p.availableCash)}
            {p.reservedCash > 0 && (
              <div className="text-xs font-normal text-slate-500">{money(p.reservedCash)} reserved for orders</div>
            )}
          </>} />
        <Stat label="Total return" value={`${money(totalReturn)} (${pct(totalReturnPct)})`}
          className={signColor(totalReturn)} />
        <Stat label="Realized P&L" value={money(p.realizedPnl)} className={signColor(p.realizedPnl)} />
      </div>
      {p.pricesIncomplete && (
        <p className="text-sm text-amber-700">Some prices are unavailable right now. Totals leave those holdings out.</p>
      )}

      <Card className="overflow-x-auto p-0">
        {holdings.length === 0 ? (
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
              {holdings.map((h) => (
                <tr key={h.symbol} className="border-b border-slate-100 last:border-0">
                  <td className="px-4 py-2">
                    <Link to={`/stocks/${h.symbol}`} className="font-medium hover:underline">{h.symbol}</Link>
                    <div className="max-w-48 truncate text-xs text-slate-500">{h.name}</div>
                  </td>
                  <td className="px-4 py-2 text-right">
                    {qty(h.qty)}
                    {h.reservedQty > 0 && <div className="text-xs text-slate-500">{qty(h.reservedQty)} in orders</div>}
                  </td>
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
