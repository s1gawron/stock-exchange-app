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
