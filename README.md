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

- Java 21
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

## API (Phase 1)

All `/api/**` endpoints except register/login/csrf need a session. State-changing requests need the `X-XSRF-TOKEN` header (value of the `XSRF-TOKEN` cookie; `GET /api/auth/csrf` sets it).

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
| POST | `/api/orders` | `{symbol, side: BUY\|SELL, qty}` + `Idempotency-Key` header. Market orders, whole shares |
| GET | `/api/orders?page=&size=` | Order history, newest first |
| GET | `/api/portfolio` | Cash, holdings, P&L, total return |

Business rejections (market closed, not enough cash/shares, stale or missing quote) return **200** with `status: "REJECTED"` and a `rejectReason`.

## Tests

```bash
cd backend && ./mvnw verify        # starts Postgres and Redis with Testcontainers
cd frontend && npm run lint && npm run build
```
