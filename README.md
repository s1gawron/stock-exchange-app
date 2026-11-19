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

Host ports 5433 and 6380 avoid clashes with other local Postgres/Redis instances. Change them with `DB_PORT` / `REDIS_PORT` in `.env`.

## Tests

```bash
cd backend && ./mvnw verify        # starts Postgres and Redis with Testcontainers
cd frontend && npm run lint && npm run build
```
