#!/usr/bin/env node
// Finds how many symbols one Finnhub WebSocket connection accepts on our plan.
// Subscribes liquid US symbols one by one, records error messages and which symbols deliver trades.
// Run during US market hours (15:30–22:00 Polish time) with the backend STOPPED: the free plan may allow only one
// connection per key. Reads FINNHUB_API_KEY from the repo-root .env and never prints it.
// Usage: node scripts/finnhub-ws-limit.mjs [maxSymbols=120] [listenSeconds=40]
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const key = readFileSync(join(root, '.env'), 'utf8').match(/^FINNHUB_API_KEY=(.+)$/m)?.[1]?.trim()
if (!key) {
  console.error('FINNHUB_API_KEY is not set in .env')
  process.exit(1)
}

// Liquid US stocks and ETFs: nearly all trade every few seconds during regular hours.
const SYMBOLS = `AAPL MSFT NVDA AMZN GOOGL META TSLA AVGO JPM V MA UNH XOM LLY JNJ PG HD COST ABBV MRK
  CVX PEP KO ADBE CRM NFLX AMD INTC CSCO ORCL WMT BAC WFC DIS MCD T VZ PFE TMO ABT
  QCOM TXN IBM GE CAT BA NKE SBUX PYPL UBER SHOP SQ PLTR SNOW F GM C GS MS
  SCHW BLK AXP MU AMAT LRCX KLAC ADI MRVL PANW CRWD NOW INTU SPY QQQ IWM DIA XLF XLE
  XLK EEM GLD SLV TLT HYG LQD VXX ARKK SOXL TQQQ SQQQ UVXY KRE XLV XLI XLP XLU
  SMH COIN MARA RIOT RIVN LCID SOFI HOOD DKNG ROKU SNAP PINS ZM DOCU ABNB DASH
  LYFT BABA JD PDD NIO BIDU TSM ASML SONY TM`.split(/\s+/).filter(Boolean)

const maxSymbols = Math.min(Number(process.argv[2] ?? 120), SYMBOLS.length)
const listenSeconds = Number(process.argv[3] ?? 40)
const symbols = SYMBOLS.slice(0, maxSymbols)
const order = new Map(symbols.map((s, i) => [s, i + 1])) // subscription position, 1-based

const errors = []
const tradeCount = new Map()
let subscribed = 0

const ws = new WebSocket(`wss://ws.finnhub.io?token=${encodeURIComponent(key)}`)
ws.onerror = () => console.error('WebSocket error (details hidden: the URL contains the key)')
ws.onclose = (e) => console.log(`connection closed: code ${e.code}${e.reason ? ` (${e.reason})` : ''} after ${subscribed} subscriptions`)
ws.onmessage = (e) => {
  const msg = JSON.parse(e.data)
  if (msg.type === 'trade') {
    for (const t of msg.data ?? []) tradeCount.set(t.s, (tradeCount.get(t.s) ?? 0) + 1)
  } else if (msg.type === 'error') {
    errors.push({ afterSubscriptions: subscribed, msg: msg.msg })
    if (errors.length === 1) console.log(`first error after ${subscribed} subscriptions: ${msg.msg}`)
  }
}

await new Promise((resolve, reject) => {
  ws.onopen = resolve
  setTimeout(() => reject(new Error('connect timeout')), 10_000)
})
console.log(`connected; subscribing ${symbols.length} symbols, one every 150 ms`)
for (const s of symbols) {
  if (ws.readyState !== WebSocket.OPEN) break
  ws.send(JSON.stringify({ type: 'subscribe', symbol: s }))
  subscribed++
  await new Promise((r) => setTimeout(r, 150))
}
console.log(`subscribed ${subscribed}; listening ${listenSeconds} s for trades...`)
await new Promise((r) => setTimeout(r, listenSeconds * 1000))

const withTrades = [...tradeCount.keys()].filter((s) => order.has(s)).sort((a, b) => order.get(a) - order.get(b))
const highest = withTrades.length ? order.get(withTrades.at(-1)) : 0
const silent = symbols.filter((s) => !tradeCount.has(s))
console.log('\n--- result')
const kinds = [...new Set(errors.map((e) => e.msg))]
console.log(`errors: ${errors.length ? `${errors.length} (${kinds.map((k) => `"${k}"`).join(', ')}), first after ${errors[0].afterSubscriptions} subscriptions` : 'none'}`)
console.log(`symbols with trades: ${withTrades.length} of ${subscribed}`)
console.log(`highest subscription position that delivered trades: ${highest}`)
console.log(`silent symbols by position: ${silent.map((s) => `${order.get(s)}:${s}`).join(' ') || 'none'}`)
ws.close()
