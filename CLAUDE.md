# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack stock exchange simulation app. Users can browse US/LSE stock listings (via Finnhub API) and execute buy/sell transactions. Stack: Spring Boot 4 backend + React 19 + TypeScript frontend.

## Commands

### Backend (`cd backend`)
```bash
./mvnw clean install          # Build and run all tests
./mvnw test                   # Run tests only
./mvnw test -Dtest=ClassName  # Run a single test class
./mvnw spring-boot:run -Dspring-boot.run.profiles=local  # Run locally
```

### Frontend (`cd frontend`)
```bash
npm install       # Install dependencies
npm run dev       # Dev server at http://localhost:5173
npm run build     # Type-check + production build
npm run lint      # ESLint
```

### Full Stack (Docker)
```bash
docker-compose up  # Starts backend, frontend, PostgreSQL, Redis
```
Requires `FINNHUB_TOKEN` env var to be set (Finnhub API key).

## Architecture

### Backend (`backend/src/main/java/com/s1gawron/stockexchange/`)

**Domain-driven package layout** — each domain has its own controller/service/dao/model/dto/exception sub-packages:
- `user/` — registration, login, wallet management, scheduled end-of-day job
- `stock/` — stock data via Finnhub REST API and Wikipedia scraping (S&P500, DJI, NASDAQ100 index constituents)
- `transaction/` — buy/sell transaction creation and processing
- `trade/` + `websocket/` — real-time trade data via Finnhub WebSocket (gated behind `websockets` Spring profile)
- `security/` — JWT filter and service (jjwt, HMAC-SHA256, 1h expiry)
- `shared/` — `AbstractErrorHandlerController` base class and `ErrorResponse` record used across all controllers
- `configuration/` — Security, cache (Redis), WebSocket, and core bean configuration

**Key patterns:**
- **DAOs use manual Hibernate Criteria API** (not Spring Data repositories). `EntityManager` + `CriteriaBuilder` + generated JPA metamodel classes (e.g. `User_`).
- **Strategy pattern** for transactions: `TransactionCreatorStrategy` / `TransactionProcessorStrategy` interfaces, with `Purchase*` and `Sell*` implementations.
- **Validators** are singleton enums: `TransactionRequestDTOValidator.I`, `UserRegisterDTOValidator.I`.
- **`UserContextProvider.I`** — singleton enum to get the current authenticated `User` from Spring Security context.
- Controllers extend domain-specific error handler controllers (e.g. `StockErrorHandlerController`), which all extend `AbstractErrorHandlerController`.
- The `User` entity directly implements `UserDetails`.

**Database:** PostgreSQL 17, schema `stock-exchange-app`, migrations via Flyway (`V1__init.sql`). Tables: `public__user`, `public__user_wallet`, `public__user_stock`, `public__transaction`. Hibernate `ddl-auto: none`.

**Caching (Redis):** Three caches — `stockSearchCache` (1d TTL), `stockDataCache` (15s TTL), `indexCompaniesCache` (30d TTL).

**Spring profiles:**
- `local` — connects to `localhost:5432`, no Redis host override, CORS allows `http://localhost:5173`
- `docker` — connects to `stock-exchange-app-database:5432`, Redis at `stock-exchange-app-redis`, CORS allows `http://localhost:3000`

**API structure:** Public endpoints at `/api/public/**`, authenticated endpoints at `/api/user/**` and `/api/transaction/**`. Swagger UI at `/swagger-ui/index.html`.

### Frontend (`frontend/src/`)

- **`page/`** — one directory per route, each with a `.tsx` component and `styles.module.css`
- **`component/`** — reusable UI components with co-located CSS Modules
- **`dto/`** — TypeScript interfaces for request/response shapes
- **`util/`** — service layer organized by domain (user, stocklistings, transaction); each domain has a `*Service.ts` (axios calls) and a `*ServiceUrlProvider.ts` (URL builders)

**Auth:** JWT stored in `sessionStorage` via `AuthUtil.ts`. No route guards — auth checks are done inside page components.

**API base URL** built from Vite env vars in `UrlProvider.ts`: `VITE_API_SERVICE_SCHEME`, `VITE_API_SERVICE_HOST`, `VITE_API_SERVICE_PORT` (defaults: `http`, `localhost`, `8080`).

**No frontend tests** — there is no test runner configured.

## Commit Convention

Prefix commits with the affected area in brackets:
- `[BACKEND]` for backend changes
- `[FRONTEND]` for frontend changes
