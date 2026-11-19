# Stock Simulator

Paper-trading app: buy and sell stocks with virtual money at real market prices (data from [Finnhub](https://finnhub.io)).

- **Plan and roadmap:** [docs/PLAN.md](docs/PLAN.md)

## Layout

| Path | What |
|---|---|
| `backend/` | Spring Boot 4 (Java 21, Maven) |
| `frontend/` | React + TypeScript + Vite, Tailwind, TanStack Query |
| `compose.yaml` | Local PostgreSQL 16 and Redis 7 |
| `.github/workflows/ci.yml` | CI: backend tests, frontend lint and build |

## Requirements

- Java 21 (Maven must run on it too). If your default is older, set it per terminal:
  `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` (macOS). The build stops with this hint otherwise.
- Node 22 (`frontend/.nvmrc`)
- Docker (for Compose and for Testcontainers in backend tests)

## Run locally

```bash
# 1. Secrets
cp .env.example .env          # then set FINNHUB_API_KEY

# 2. Infrastructure (Postgres on localhost:5433, Redis on localhost:6380)
docker compose up -d

# 3. Backend on http://localhost:8080
cd backend && ./mvnw spring-boot:run

# 4. Frontend on http://localhost:5173 (proxies /api and /actuator to the backend)
cd frontend && npm install && npm run dev
```

The backend reads the repo-root `.env` automatically. Do not commit `.env`.

No Finnhub key yet, or the US market is closed? Set `MARKET_DATA_PROVIDER=fake` in `.env`. The fake provider has 8 symbols (AAPL, MSFT, NVDA, AMZN, GOOGL, TSLA, META, SPY), slowly moving prices, and reports the market as open.

With the real provider, market orders are **rejected outside US regular hours**. Open/closed comes from Finnhub's market status (cached 60 s); when Finnhub is unreachable, a local calendar (09:30–16:00 New York, Mon–Fri, holidays from Finnhub + config) decides.

`scripts/finnhub-check.sh` shows which Finnhub endpoints your plan allows (uses 7 API calls).

Host ports 5433 and 6380 avoid clashes with other local Postgres/Redis instances. Change them with `DB_PORT` / `REDIS_PORT` in `.env`.

## API

All `/api/**` endpoints except register/login/csrf need a session. Sessions live in Redis (cookie `SESSION`, HttpOnly, SameSite=Lax, 30 min idle timeout), so logins survive backend restarts. State-changing requests need the `X-XSRF-TOKEN` header (value of the `XSRF-TOKEN` cookie; `GET /api/auth/csrf` sets it).

| Method | Path | Notes |
|---|---|---|
| GET | `/api/auth/csrf` | Sets the `XSRF-TOKEN` cookie |
| POST | `/api/auth/register` | `{email, displayName, password}`; opens an account with $100,000 and logs in |
| POST | `/api/auth/login` | `{email, password}` |
| POST | `/api/auth/logout` | |
| GET | `/api/auth/me` | Current user |
| GET | `/api/market/status` | US regular session open/closed |
| GET | `/api/instruments?q=` | Search by symbol or name |
| GET | `/api/instruments/{symbol}` | Details (company profile loaded once from Finnhub) |
| GET | `/api/quotes/{symbol}` | Latest quote (cached 5 s in Redis) |
| POST | `/api/orders` | `{symbol, side: BUY\|SELL, type: MARKET\|LIMIT\|STOP, qty, limitPrice?, stopPrice?, timeInForce?: DAY\|GTC}` + `Idempotency-Key` header. Whole shares |
| POST | `/api/orders/{id}/cancel` | Cancel an open order (releases its reservation) |
| GET | `/api/orders?status=&page=&size=` | Orders, newest first; `status=OPEN` for waiting orders |
| GET | `/api/portfolio` | Cash (available / reserved), holdings, P&L, total return |
| GET | `/api/watchlist` | Watched symbols with last price |
| PUT / DELETE | `/api/watchlist/{symbol}` | Add / remove (max 50) |
| GET | `/api/analytics` | Return, max drawdown, best/worst day, vs SPY, daily snapshots |
| GET | `/api/leaderboard?limit=` | Ranking by return since opening, as of the last close |
| GET / POST | `/api/competitions` | My competitions / create `{name, startsOn, endsOn}` |
| POST | `/api/competitions/join` | `{code}` — until the start day's US open |
| GET | `/api/competitions/{id}` | Details and standings (members only) |
| POST | `/api/competitions/{id}/leave` | Before the start; the creator leaving deletes it |
| WS | `/ws` | Send `{"type":"subscribe","symbols":[...]}`; receive `{"type":"price",...}` and `{"type":"order","order":...}` |

Business rejections (market closed, not enough cash/shares, stale or missing quote) return **200** with `status: "REJECTED"` and a `rejectReason`.

**Order rules**
- Market orders: only during US regular hours; fill at the last price.
- Limit / stop orders: can be placed any time and wait as `OPEN`; they fill only during regular hours. BUY reserves cash (limit price, or stop price + 5%), SELL reserves shares. `DAY` orders expire at the close of their session; `GTC` orders wait until filled or cancelled.
- Commissions are configurable in `application.properties` (`trading.fees.*`), zero by default. No slippage.

**Performance data** comes from a snapshot of every account after each US trading day (16:30 New York = 22:30 Polish time). If the backend is down then, it catches up on the next start, but only before the next session opens.

## Tests

```bash
cd backend && ./mvnw verify        # starts Postgres and Redis with Testcontainers
cd frontend && npm run lint && npm run build
```
