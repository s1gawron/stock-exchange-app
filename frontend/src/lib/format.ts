const usd = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })
const qtyFmt = new Intl.NumberFormat('en-US', { maximumFractionDigits: 6 })

export const money = (v: number | null | undefined) => (v == null ? '—' : usd.format(v))
export const qty = (v: number) => qtyFmt.format(v)
export const pct = (v: number | null | undefined) =>
  v == null ? '—' : `${v > 0 ? '+' : ''}${v.toFixed(2)}%`
export const dateTime = (iso: string) => new Date(iso).toLocaleString()

/** Tailwind text color for a signed number. */
export const signColor = (v: number | null | undefined) =>
  v == null || v === 0 ? 'text-slate-600' : v > 0 ? 'text-emerald-600' : 'text-rose-600'

export const rejectText: Record<string, string> = {
  MARKET_CLOSED: 'Market is closed',
  NO_QUOTE: 'No price available',
  STALE_QUOTE: 'Price is too old',
  INSUFFICIENT_FUNDS: 'Not enough cash',
  INSUFFICIENT_SHARES: 'Not enough shares',
}

export const orderTypeText = (o: { type: string; limitPrice: number | null; stopPrice: number | null }) =>
  o.type === 'LIMIT' ? `Limit ${money(o.limitPrice)}` : o.type === 'STOP' ? `Stop ${money(o.stopPrice)}` : 'Market'

/** "2026-11-19" -> "Nov 19, 2026" (dates from the API are New York trading days, no time zone shift). */
export const day = (iso: string | null | undefined) =>
  iso ? new Date(`${iso}T12:00:00Z`).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', timeZone: 'UTC' }) : '—'


/** Label for trades outside the regular session; null during regular hours. */
export const sessionLabel = (s: string | null | undefined) =>
  s === 'PRE_MARKET' ? 'Pre-market' : s === 'AFTER_HOURS' ? 'After hours' : null

/** Tailwind classes for competition status badges. */
export const statusBadge: Record<'UPCOMING' | 'ACTIVE' | 'FINISHED', string> = {
  UPCOMING: 'bg-sky-100 text-sky-800',
  ACTIVE: 'bg-emerald-100 text-emerald-800',
  FINISHED: 'bg-slate-100 text-slate-600',
}
