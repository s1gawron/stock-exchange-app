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
export type OrderType = 'MARKET' | 'LIMIT' | 'STOP'
export type TimeInForce = 'DAY' | 'GTC'
export type OrderStatus = 'OPEN' | 'FILLED' | 'REJECTED' | 'CANCELLED' | 'EXPIRED'

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
  type: OrderType
  qty: number
  limitPrice: number | null
  stopPrice: number | null
  timeInForce: TimeInForce | null
  status: OrderStatus
  rejectReason: RejectReason | null
  reservedCash: number
  reservedQty: number
  expiresAt: string | null
  createdAt: string
  updatedAt: string
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
  reservedQty: number
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
  reservedCash: number
  availableCash: number
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

export type WatchlistItem = {
  symbol: string
  name: string
  price: number | null
  change: number | null
  changePercent: number | null
}

export type AnalyticsDay = {
  day: string
  equity: number
  dayChangePercent: number | null
  returnPercent: number
  pricesComplete: boolean
}

export type Analytics = {
  benchmarkSymbol: string
  firstDay: string | null
  lastDay: string | null
  tradingDays: number
  returnPercent: number | null
  periodReturnPercent: number | null
  benchmarkReturnPercent: number | null
  vsBenchmarkPercent: number | null
  maxDrawdownPercent: number | null
  maxDrawdownPeakDay: string | null
  maxDrawdownTroughDay: string | null
  bestDay: AnalyticsDay | null
  worstDay: AnalyticsDay | null
  days: AnalyticsDay[]
}

export type LeaderboardEntry = { rank: number; displayName: string; returnPercent: number; equity: number; me: boolean }

export type Leaderboard = { asOf: string | null; players: number; top: LeaderboardEntry[]; me: LeaderboardEntry | null }

export type CompetitionStatus = 'UPCOMING' | 'ACTIVE' | 'FINISHED'

export type Competition = {
  id: number
  name: string
  code: string
  startsOn: string
  endsOn: string
  status: CompetitionStatus
  members: number
  creator: boolean
}

export type CompetitionDetails = {
  competition: Competition
  asOf: string | null
  standings: { rank: number; displayName: string; returnPercent: number | null; me: boolean }[]
}
