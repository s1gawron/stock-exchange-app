// Live prices and order notifications over one WebSocket per tab (see components/LiveProvider.tsx).
import { createContext, useCallback, useContext, useEffect, useSyncExternalStore } from 'react'
import type { Order, TradingSession } from './types'

export type Tick = { price: number; timestamp: string; session: TradingSession }

export type LiveContextValue = {
  register: (symbols: string[]) => () => void
  subscribePrices: (listener: () => void) => () => void
  getPrice: (symbol: string) => Tick | undefined
  connected: boolean
  lastOrder: Order | null
}

export const LiveContext = createContext<LiveContextValue | null>(null)

function useLive() {
  const ctx = useContext(LiveContext)
  if (!ctx) throw new Error('useLive outside LiveProvider')
  return ctx
}

/** Latest live prices for the given symbols (registers them for streaming while mounted). */
export function useLivePrices(symbols: string[]): Record<string, Tick | undefined> {
  const live = useLive()
  const { register } = live
  const key = [...new Set(symbols)].sort().join(',')

  useEffect(() => {
    if (!key) return
    return register(key.split(','))
  }, [key, register])

  const snapshot = useCallback(
    () => (key ? key.split(',').map((s) => `${s}:${live.getPrice(s)?.price ?? ''}`).join('|') : ''),
    [key, live],
  )
  useSyncExternalStore(live.subscribePrices, snapshot)

  const out: Record<string, Tick | undefined> = {}
  if (key) for (const s of key.split(',')) out[s] = live.getPrice(s)
  return out
}

export function useLiveStatus() {
  const { connected, lastOrder } = useLive()
  return { connected, lastOrder }
}
