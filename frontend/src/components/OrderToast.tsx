import { useEffect, useState } from 'react'
import { money, qty, rejectText } from '../lib/format'
import { useLiveStatus } from '../lib/live'
import type { Order } from '../lib/types'

function text(o: Order): string {
  const what = `${o.side === 'BUY' ? 'Buy' : 'Sell'} ${qty(o.qty)} ${o.symbol}`
  switch (o.status) {
    case 'FILLED':
      return `${what} filled at ${money(o.fill?.price)}`
    case 'EXPIRED':
      return `${what} expired`
    case 'CANCELLED':
      return `${what} cancelled`
    case 'REJECTED':
      return `${what} rejected: ${rejectText[o.rejectReason ?? ''] ?? o.rejectReason}`
    default:
      return `${what} updated`
  }
}

/** Shows fills/expiries of waiting orders pushed by the server, for 6 seconds. */
export function OrderToast() {
  const { lastOrder } = useLiveStatus()
  const [dismissed, setDismissed] = useState<Order | null>(null)

  useEffect(() => {
    if (!lastOrder) return
    const t = setTimeout(() => setDismissed(lastOrder), 6000)
    return () => clearTimeout(t)
  }, [lastOrder])

  const visible = lastOrder && lastOrder !== dismissed ? lastOrder : null
  if (!visible) return null
  const ok = visible.status === 'FILLED'
  return (
    <div role="status" aria-live="polite"
      className={`fixed bottom-4 right-4 left-4 z-10 rounded-lg border px-4 py-3 text-sm shadow-lg sm:left-auto ${
        ok ? 'border-emerald-200 bg-emerald-50 text-emerald-800' : 'border-slate-200 bg-white text-slate-800'}`}>
      <span>{text(visible)}</span>
      <button type="button" aria-label="Dismiss" onClick={() => setDismissed(visible)}
        className="ml-3 text-slate-400 hover:text-slate-900">✕</button>
    </div>
  )
}
