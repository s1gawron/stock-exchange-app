export type Me = { id: number; email: string; displayName: string }

export type MarketStatus = { open: boolean; description: string }

export type Instrument = {
  symbol: string
  displaySymbol: string
  name: string
  type: string
  currency: string | null
  exchange: string | null
  logoUrl: string | null
  webUrl: string | null
  industry: string | null
  active: boolean
}

export type Quote = {
  symbol: string
  price: number
  change: number
  changePercent: number
  high: number
  low: number
  open: number
  previousClose: number
  timestamp: string
}

export type Side = 'BUY' | 'SELL'

export type RejectReason =
  | 'MARKET_CLOSED'
  | 'NO_QUOTE'
  | 'STALE_QUOTE'
  | 'INSUFFICIENT_FUNDS'
  | 'INSUFFICIENT_SHARES'

export type Order = {
  id: number
  symbol: string
  side: Side
  type: string
  qty: number
  status: 'FILLED' | 'REJECTED'
  rejectReason: RejectReason | null
  createdAt: string
  fill: {
    price: number
    grossAmount: number
    fee: number
    realizedPnl: number | null
    quoteTime: string
    executedAt: string
  } | null
}

export type Holding = {
  symbol: string
  name: string
  qty: number
  avgCost: number
  costBasis: number
  lastPrice: number | null
  marketValue: number | null
  unrealizedPnl: number | null
  unrealizedPnlPercent: number | null
}

export type Portfolio = {
  currency: string
  cash: number
  marketValue: number
  equity: number
  costBasis: number
  unrealizedPnl: number
  realizedPnl: number
  deposits: number
  totalReturn: number
  totalReturnPercent: number
  pricesIncomplete: boolean
  holdings: Holding[]
}
