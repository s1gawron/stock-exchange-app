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

We use Finnhub because we already have an account. **Plan: free tier.**

Availability checked on 2026-11-19 with `scripts/finnhub-check.sh` (re-run it after a plan change):

| Need | Finnhub feature | Free tier | How we use it |
|---|---|---|---|
| Current price | REST `GET /quote?symbol=` | ✅ | Quotes, cached 5 s in Redis |
| Live prices | WebSocket `wss://ws.finnhub.io` | ✅ (symbol limit: verify in Phase 2) | Phase 2 ingestor, one upstream connection |
| Symbol list / search | REST `GET /stock/symbol?exchange=US`, `GET /search` | ✅ | Daily import (~27.7k US instruments), search in our DB |
| Company info | REST `GET /stock/profile2?symbol=` | ✅ | Loaded once per symbol, stored |
| Market open/closed | REST `GET /stock/market-status?exchange=US` | ✅ | Main source, cached 60 s; local calendar is the fallback |
| Holidays / early closes | REST `GET /stock/market-holiday?exchange=US` | ✅ | Loaded at startup + daily into the fallback calendar |
| Historical candles | REST `GET /stock/candle` | ❌ 403 | Not used — charts are out of scope |
| Splits | REST `GET /stock/split` | ❌ 403 | Not used — split handling is out of scope |
| Dividends | REST `GET /stock/dividend` | not checked | Not used — dividends are out of scope |

**Open questions**
- [x] Which endpoints our plan includes — see table above.
- [ ] Max WebSocket symbols per connection on the free tier (check in Phase 2). REST limit: ~60/min (our client caps at 55).
- [ ] Terms of use: the free plan is reported as personal use only. Until verified, keep the app **private** (us and maybe a few friends); public launch needs a plan that allows display to end users.

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
| Backend | Java 21 + Spring Boot 4 (Web MVC, WebSocket, JDBC/JdbcClient, Security), Maven |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Cache / pub-sub | Redis (latest quotes, fan-out to WebSocket nodes, leaderboards) |
| Auth | Built-in: Spring Security, email + password (BCrypt), session cookie, SPA CSRF |
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
- `marketdata` — Finnhub client, ingestor, quote cache, symbol catalog.
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
ledger_entries   (id, account_id, type DEPOSIT|BUY|SELL|FEE,
                  symbol, cash_amount, qty_amount, ref_id, created_at)
positions        (account_id, symbol, qty, avg_cost, PK(account_id, symbol))
watchlist_items  (user_id, symbol)
```

---

## 7. Roadmap

### Phase 0 — Project setup
- [x] Create repository
- [x] Monorepo layout: `backend/`, `frontend/`, root `compose.yaml` (`infra/` later, for deploy files)
- [x] Docker Compose: Postgres 16 (host port 5433), Redis 7 (host port 6380)
- [x] Spring Boot skeleton (Java 21, Flyway baseline, `/actuator/health`, Finnhub config binding)
- [x] Context-load test with Testcontainers (Postgres + Redis)
- [x] React + Vite skeleton (Tailwind, TanStack Query, dev proxy, backend status page)
- [x] `.env` handling for `FINNHUB_API_KEY` (Compose + `spring.config.import`)
- [x] CI workflow (GitHub Actions) — not run yet, no remote
- [ ] Create remote repository and confirm CI is green
- [ ] Answer the Finnhub open questions in section 2

### Phase 1 — MVP
- [x] Finnhub REST client with rate limiter (55/min) + Redis quote cache (5 s); key only in `X-Finnhub-Token` header
- [x] Fake `MarketDataProvider` (8 symbols, slowly moving prices, switchable market open/closed)
- [x] Market status from Finnhub (cached 60 s) with local US calendar fallback; holidays loaded from Finnhub daily
- [x] Symbol catalog import (daily 06:00 New York + on startup when stale or provider changed)
- [x] Sign-up / login / logout (session cookie, CSRF)
- [x] Create account with starting cash ($100,000 as DEPOSIT ledger entry)
- [x] Symbol search + stock page with quote
- [x] Market BUY / SELL with ledger, positions, idempotency; reject when market closed / stale quote
- [x] Concurrency tests (parallel buys can't overspend; parallel retries with one key = one order)
- [x] Portfolio view: holdings, market value, unrealized/realized P&L (average cost), total return
- [x] Transaction history (orders incl. rejected, paged)
- [x] Smoke test against real Finnhub (catalog 27,694 instruments, 62 holiday days, quote, MARKET_CLOSED rejection)
- [ ] Place a real order during US market hours (15:30–22:00 Polish time)

### Phase 2 — Live trading feel
- [ ] Finnhub WebSocket ingestor → Redis latest quote
- [ ] Browser WebSocket: live prices for watched/held symbols
- [ ] Watchlists
- [ ] Market-hours rules (reject or queue when closed)
- [ ] Limit and stop orders + background matching worker
- [ ] Commissions and slippage (configurable)

### Phase 3 — Social and accuracy
- [ ] Daily portfolio snapshots (equity curve)
- [ ] Leaderboard (Redis sorted sets)
- [ ] Competitions (time-boxed, same starting cash)
- [ ] Analytics: return %, max drawdown, benchmark vs. S&P 500 ETF
- [ ] Short selling (maybe)

---

## 8. Risks

| Risk | Mitigation |
|---|---|
| Finnhub plan limits (rate, symbols, endpoints) | Cache, single upstream WS, fake provider, verify early |
| Finnhub terms forbid showing data to others | Check terms before public launch; keep private until clear |
| Double spend under concurrent orders | Row locking + idempotency keys + Testcontainers tests |
| Splits distort positions (e.g. 4:1 split → share count stays, price drops ~75%, shows as a big loss) | **Accepted** — split handling is out of scope. If it hits a user, fix by hand: one SQL adjustment of `positions.qty` + a ledger entry |
| Trading on stale prices | Quote timestamp check, market-status check |

---

## 9. Decision log

| Date | Decision | Reason |
|---|---|---|
| 2026-11-19 | Market data from Finnhub | Existing account |
| 2026-11-19 | Modular monolith, Spring Boot + React + PostgreSQL + Redis | See section 3 |
| 2026-11-19 | Spring Boot 4.1 instead of 3.x, Maven as build tool | 4.x is the current release line; Maven already installed |
| 2026-11-19 | Dev host ports 5433 (Postgres) and 6380 (Redis) | 5432/6379 are often taken by other local services |
| 2026-11-19 | Single repo-root `.env`, read by Compose and by Spring | One place for secrets in local dev |
| 2026-11-19 | Auth: built-in Spring Security sessions (no Keycloak for now) | Fastest MVP, no extra service |
| 2026-11-19 | Market orders outside US regular hours are always rejected (real provider) | Realism; fake provider has its own open/closed switch for testing |
| 2026-11-19 | JDBC (`JdbcClient`) instead of JPA | Explicit SQL for row locks and ledger writes |
| 2026-11-19 | Market status from Finnhub `/stock/market-status` (free tier ✅), local calendar only as fallback | Free tier includes it; fallback keeps trading rules sane during outages / rate limits |
| 2026-11-19 | Holidays from Finnhub `/stock/market-holiday`, merged with config list | No yearly manual upkeep |
| 2026-11-19 | No historical candles / splits from Finnhub (403 on free tier) | See next two rows |
| 2026-11-19 | **Drop price charts** from scope | Not needed; free tier has no candles |
| 2026-11-19 | **Drop stock split handling** from scope | Not needed; free tier has no split data; rare event, fix by hand if needed |
| 2026-11-19 | **Drop dividends** from scope | Not needed. Effect: returns of dividend-paying stocks are slightly lower than in reality (price drops on ex-date, no cash paid) |
| 2026-11-19 | Keep the app private while on the free tier | Free plan reported as personal use; verify terms before going public |
| 2026-11-19 | Whole shares only, no fees, average-cost P&L | Keep MVP simple; schema already allows fractions and fees |
| 2026-11-19 | Rejected orders are stored and returned with HTTP 200 | Idempotent retries see the same result; history shows rejections |
