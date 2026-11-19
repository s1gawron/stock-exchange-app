# Stock Simulator — Project Plan

A paper-trading app: users get virtual cash and buy or sell stocks at real market prices from **Finnhub**.

> Living document. Tick the checkboxes as we go and record decisions in the [Decision log](#9-decision-log).

---

## 1. Goals and non-goals

**Goals**
- Users trade US stocks with virtual money at real (or slightly delayed) prices.
- Correct, auditable accounting: every cent and every share can be traced.
- Live prices in the UI.
- Leaderboards and competitions (later phase).

**Non-goals (for now)**
- Real money, real brokerage integration.
- Options, futures, crypto, forex.
- Mobile native apps (responsive web only).

---

## 2. Market data: Finnhub

We use Finnhub because we already have an account.

| Need | Finnhub feature | Notes |
|---|---|---|
| Current price | REST `GET /quote?symbol=` | Snapshot: current, high, low, open, prev close |
| Live prices | WebSocket `wss://ws.finnhub.io?token=…` (`subscribe` per symbol) | Trade ticks. One upstream connection for the whole app. |
| Symbol search | REST `GET /search?q=` and `GET /stock/symbol?exchange=US` | Load the symbol list into our DB once a day |
| Company info | REST `GET /stock/profile2?symbol=` | Name, logo, currency, exchange. Cache it. |
| Historical candles | REST `GET /stock/candle` | **Verify** if our plan includes it. If not, build our own bars from WebSocket ticks or pick a second source. |
| Market open/closed | REST `GET /stock/market-status?exchange=US` | Check if available on our plan |
| Splits / dividends | REST `/stock/split`, `/stock/dividend` | Needed in Phase 3. Check plan availability. |

**Open questions — verify in the Finnhub dashboard/docs before we build on them:**
- [ ] Which endpoints does our current plan include (candles, market status, splits, dividends)?
- [ ] Rate limit on our plan (REST calls/min) and max WebSocket symbols per connection.
- [ ] Terms of use: are we allowed to **display** Finnhub data to other users of our app? Free/personal plans may forbid redistribution. If the app stays private or for friends only, record that decision.

**Rules for our code**
- The API key lives only in the backend (`FINNHUB_API_KEY` env var). Never send it to the browser, never commit it.
- All Finnhub access goes through one interface (`MarketDataProvider`) so that we can add a fake provider (for tests and outside market hours) or swap vendors.
- Only the ingestor talks to the Finnhub WebSocket. It subscribes to the **union** of symbols that users watch or hold, stays within the symbol limit, and writes the latest quote to Redis.
- REST calls go through a rate limiter and a cache.

---

## 3. Tech stack

| Layer | Choice |
|---|---|
| Frontend | React + TypeScript + Vite, TanStack Query, Tailwind CSS |
| Charts | TradingView Lightweight Charts |
| Backend | Java 21 + Spring Boot 3 (Web, WebSocket, Data JPA/JDBC, Security) |
| Database | PostgreSQL 16 (TimescaleDB extension optional for price bars) |
| Migrations | Flyway |
| Cache / pub-sub | Redis (latest quotes, fan-out to WebSocket nodes, leaderboards) |
| Auth | Keycloak (self-hosted) or managed provider — **decide** |
| Tests | JUnit 5, Testcontainers (Postgres, Redis), Playwright (E2E) |
| Local dev | Docker Compose |
| Deploy | Single VPS / Fly.io first |
| Observability | OpenTelemetry, Prometheus, Grafana |

---

## 4. Architecture

Modular monolith. One backend app with clear modules. The market-data ingestor can become a separate process later.

```
            ┌──────────── Browser (React SPA) ────────────┐
            │   REST (commands/queries)   WebSocket (prices, fills)
            └───────────────┬─────────────────────────────┘
                     Spring Boot app
   ┌──────────┬───────────┬────────────┬──────────────┬─────────────┐
   │ accounts │ market-   │ trading    │ portfolio /  │ leaderboard │
   │ & auth   │ data      │ (orders,   │ ledger       │             │
   │          │           │ execution) │              │             │
   └──────────┴─────┬─────┴─────┬──────┴──────┬───────┴─────────────┘
                    │           │             │
   Finnhub WS/REST ─┘     Redis (quotes,   PostgreSQL (source of truth)
                          pub/sub)
```

**Modules**
- `accounts` — users, virtual cash accounts.
- `marketdata` — Finnhub client, ingestor, quote cache, symbol catalog, bars.
- `trading` — orders, validation, execution, limit/stop worker.
- `portfolio` — ledger, positions, P&L, snapshots.
- `leaderboard` — rankings, competitions (Phase 3).

---

## 5. Core rules (do not break these)

1. **No floating point for money.** Use `BigDecimal` / `NUMERIC(19,4)`. Quantities are `NUMERIC` too (fractional shares possible later).
2. **Ledger is the source of truth.** Every cash or share change is an append-only `ledger_entries` row. `positions` and account balance are derived/cached and must reconcile with the ledger.
3. **One transaction per execution.** Lock the account row → check buying power / holdings → insert order, fill, ledger entries → update position → commit.
4. **Idempotent orders.** Client sends an `Idempotency-Key`. Same key = same result, no double buy.
5. **No trades on stale prices.** Every quote has a timestamp. Reject (or queue) market orders if the quote is older than the limit or the market is closed.

---

## 6. Data model (first draft)

```
users            (id, email, display_name, created_at)
accounts         (id, user_id, currency, cash_balance, version, created_at)
instruments      (symbol PK, name, exchange, currency, type, logo_url, active)
orders           (id, account_id, symbol, side BUY|SELL, type MARKET|LIMIT|STOP,
                  qty, limit_price, stop_price, status, idempotency_key UNIQUE,
                  created_at, updated_at)
fills            (id, order_id, qty, price, fee, executed_at)
ledger_entries   (id, account_id, type DEPOSIT|BUY|SELL|FEE|DIVIDEND|SPLIT,
                  symbol, cash_amount, qty_amount, ref_id, created_at)
positions        (account_id, symbol, qty, avg_cost, PK(account_id, symbol))
watchlist_items  (user_id, symbol)
price_bars       (symbol, interval, ts, open, high, low, close, volume)
```

---

## 7. Roadmap

### Phase 0 — Project setup
- [x] Create repository
- [ ] Decide: monorepo layout (`backend/`, `frontend/`, `infra/`)
- [ ] Docker Compose: Postgres, Redis
- [ ] Spring Boot skeleton (Java 21, Flyway, health endpoint)
- [ ] React + Vite skeleton
- [ ] `.env` handling for `FINNHUB_API_KEY`
- [ ] CI (build + tests)
- [ ] Answer the Finnhub open questions in section 2

### Phase 1 — MVP
- [ ] Finnhub REST client with rate limiter + cache
- [ ] Fake `MarketDataProvider` (replays recorded quotes)
- [ ] Symbol catalog import (daily job)
- [ ] Sign-up / login
- [ ] Create account with starting cash (e.g. $100,000)
- [ ] Symbol search + quote page
- [ ] Market BUY / SELL with ledger, positions, idempotency
- [ ] Concurrency tests (parallel orders on same account)
- [ ] Portfolio view: holdings, market value, unrealized/realized P&L
- [ ] Transaction history

### Phase 2 — Live trading feel
- [ ] Finnhub WebSocket ingestor → Redis latest quote
- [ ] Browser WebSocket: live prices for watched/held symbols
- [ ] Watchlists
- [ ] Market-hours rules (reject or queue when closed)
- [ ] Limit and stop orders + background matching worker
- [ ] Commissions and slippage (configurable)
- [ ] Price charts (candles from Finnhub or own bars)

### Phase 3 — Social and accuracy
- [ ] Daily portfolio snapshots (equity curve)
- [ ] Leaderboard (Redis sorted sets)
- [ ] Competitions (time-boxed, same starting cash)
- [ ] Stock splits and dividends
- [ ] Analytics: return %, max drawdown, benchmark vs. S&P 500 ETF
- [ ] Short selling (maybe)

---

## 8. Risks

| Risk | Mitigation |
|---|---|
| Finnhub plan limits (rate, symbols, endpoints) | Cache, single upstream WS, fake provider, verify early |
| Finnhub terms forbid showing data to others | Check terms before public launch; keep private until clear |
| Double spend under concurrent orders | Row locking + idempotency keys + Testcontainers tests |
| Splits corrupt positions | Corporate-action job in Phase 3, ledger entries of type SPLIT |
| Trading on stale prices | Quote timestamp check, market-status check |

---

## 9. Decision log

| Date | Decision | Reason |
|---|---|---|
| 2026-11-19 | Market data from Finnhub | Existing account |
| 2026-11-19 | Modular monolith, Spring Boot + React + PostgreSQL + Redis | See section 3 |
