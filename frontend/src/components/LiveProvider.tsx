// One WebSocket per tab for live prices and order notifications. Components register the symbols they show;
// the provider sends the union to the server and keeps the latest price per symbol.
import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { LiveContext, type LiveContextValue, type Tick } from '../lib/live'
import type { Order } from '../lib/types'

export function LiveProvider({ children }: { children: ReactNode }) {
  const qc = useQueryClient()
  const prices = useRef(new Map<string, Tick>())
  const priceListeners = useRef(new Set<() => void>())
  const wanted = useRef(new Map<string, number>()) // symbol -> ref count
  const socket = useRef<WebSocket | null>(null)
  const [connected, setConnected] = useState(false)
  const [lastOrder, setLastOrder] = useState<Order | null>(null)

  const sendSubscription = useCallback(() => {
    const ws = socket.current
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'subscribe', symbols: [...wanted.current.keys()] }))
    }
  }, [])

  useEffect(() => {
    let stopped = false
    let retry = 1000
    let timer: ReturnType<typeof setTimeout> | undefined

    const connect = () => {
      const proto = location.protocol === 'https:' ? 'wss' : 'ws'
      const ws = new WebSocket(`${proto}://${location.host}/ws`)
      socket.current = ws
      ws.onopen = () => {
        retry = 1000
        setConnected(true)
        sendSubscription()
      }
      ws.onmessage = (e) => {
        const msg = JSON.parse(e.data as string)
        if (msg.type === 'price') {
          prices.current.set(msg.symbol, { price: msg.price, timestamp: msg.timestamp })
          priceListeners.current.forEach((l) => l())
        } else if (msg.type === 'order') {
          setLastOrder(msg.order as Order)
          qc.invalidateQueries({ queryKey: ['portfolio'] })
          qc.invalidateQueries({ queryKey: ['orders'] })
        }
      }
      ws.onclose = () => {
        setConnected(false)
        socket.current = null
        if (!stopped) {
          timer = setTimeout(connect, retry)
          retry = Math.min(retry * 2, 30_000)
        }
      }
    }
    connect()
    return () => {
      stopped = true
      clearTimeout(timer)
      socket.current?.close()
    }
  }, [qc, sendSubscription])

  const register = useCallback(
    (symbols: string[]) => {
      let changed = false
      for (const s of symbols) {
        const n = wanted.current.get(s) ?? 0
        wanted.current.set(s, n + 1)
        if (n === 0) changed = true
      }
      if (changed) sendSubscription()
      return () => {
        let removed = false
        for (const s of symbols) {
          const n = (wanted.current.get(s) ?? 1) - 1
          if (n <= 0) {
            wanted.current.delete(s)
            removed = true
          } else wanted.current.set(s, n)
        }
        if (removed) sendSubscription()
      }
    },
    [sendSubscription],
  )

  const value = useMemo<LiveContextValue>(
    () => ({
      register,
      subscribePrices: (l) => {
        priceListeners.current.add(l)
        return () => priceListeners.current.delete(l)
      },
      getPrice: (s) => prices.current.get(s),
      connected,
      lastOrder,
    }),
    [register, connected, lastOrder],
  )

  return <LiveContext.Provider value={value}>{children}</LiveContext.Provider>
}
