import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/api'
import { day, money, pct, signColor } from '../lib/format'
import type { Analytics } from '../lib/types'
import { Card, ErrorBox, Stat } from '../components/ui'

export function PerformancePage() {
  const analytics = useQuery({ queryKey: ['analytics'], queryFn: () => api<Analytics>('/api/analytics') })

  if (analytics.isPending) return <p className="text-slate-500">Loading…</p>
  if (analytics.isError) return <ErrorBox error={analytics.error} />
  const a = analytics.data

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Performance</h1>
        <p className="mt-1 text-sm text-slate-600">
          {a.lastDay
            ? `From daily snapshots after the US close · ${day(a.firstDay)} – ${day(a.lastDay)} (${a.tradingDays} trading days)`
            : 'No data yet. A snapshot of your account is taken every trading day after the US close (22:30 Polish time).'}
        </p>
      </div>

      {a.lastDay && (
        <>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <Stat label="Return since start" value={pct(a.returnPercent)} className={signColor(a.returnPercent)} />
            <Stat label="Max drawdown" value={
              <>
                {pct(a.maxDrawdownPercent)}
                {a.maxDrawdownTroughDay && (
                  <div className="text-xs font-normal text-slate-500">
                    {day(a.maxDrawdownPeakDay) === '—' ? 'from start' : `from ${day(a.maxDrawdownPeakDay)}`} to {day(a.maxDrawdownTroughDay)}
                  </div>
                )}
              </>} className={signColor(a.maxDrawdownPercent)} />
            <Stat label={`You vs ${a.benchmarkSymbol}`} value={
              a.vsBenchmarkPercent == null ? '—' : (
                <>
                  <span className={signColor(a.vsBenchmarkPercent)}>{pct(a.vsBenchmarkPercent)}</span>
                  <div className="text-xs font-normal text-slate-500">
                    you {pct(a.periodReturnPercent)} · {a.benchmarkSymbol} {pct(a.benchmarkReturnPercent)} since {day(a.firstDay)}
                  </div>
                </>
              )} />
            <Stat label="Best day" value={a.bestDay ? <>{pct(a.bestDay.dayChangePercent)}<div className="text-xs font-normal text-slate-500">{day(a.bestDay.day)}</div></> : '—'}
              className={signColor(a.bestDay?.dayChangePercent)} />
            <Stat label="Worst day" value={a.worstDay ? <>{pct(a.worstDay.dayChangePercent)}<div className="text-xs font-normal text-slate-500">{day(a.worstDay.day)}</div></> : '—'}
              className={signColor(a.worstDay?.dayChangePercent)} />
            <Stat label="Last close value" value={money(a.days[0]?.equity)} />
          </div>

          <Card className="overflow-x-auto p-0">
            <table className="w-full text-sm">
              <thead className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-4 py-2">Day</th>
                  <th className="px-4 py-2 text-right">Value at close</th>
                  <th className="px-4 py-2 text-right">Day change</th>
                  <th className="px-4 py-2 text-right">Return since start</th>
                </tr>
              </thead>
              <tbody className="tabular-nums">
                {a.days.map((d) => (
                  <tr key={d.day} className="border-b border-slate-100 last:border-0">
                    <td className="px-4 py-2">
                      {day(d.day)}
                      {!d.pricesComplete && <span className="ml-2 text-xs text-amber-700" title="Some holdings had no price and were valued at cost">*</span>}
                    </td>
                    <td className="px-4 py-2 text-right">{money(d.equity)}</td>
                    <td className={`px-4 py-2 text-right ${signColor(d.dayChangePercent)}`}>{pct(d.dayChangePercent)}</td>
                    <td className={`px-4 py-2 text-right ${signColor(d.returnPercent)}`}>{pct(d.returnPercent)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        </>
      )}
    </div>
  )
}
