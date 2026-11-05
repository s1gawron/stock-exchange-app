# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack stock exchange simulation app. Users can browse US/LSE stock listings (via Finnhub API) and place buy/sell limit orders, which are filled from real-time Finnhub trades. Stack: Spring Boot 4 backend + React 19 + TypeScript frontend.

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
Frontend is served at `http://localhost:5173`, the same port as the Vite dev server, so stop `npm run dev` before `docker-compose up` (matches the backend `docker` profile CORS origin). The API URL is baked into the frontend image at build time via `--build-arg VITE_API_SERVICE_SCHEME/HOST/PORT` (defaults: `http`, `localhost`, `8080`); the nginx Content-Security-Policy `connect-src` is set from the same values.

## Architecture

### Backend (`backend/src/main/java/com/s1gawron/stockexchange/`)

**Domain-driven package layout** — each domain has its own controller/service/dao/model/dto/exception sub-packages:
- `user/` — registration, login, wallet management, favourite stocks (`user/stock/favourite/`), scheduled end-of-day job
- `stock/` — stock data via Finnhub REST API
- `transaction/` — buy/sell transaction creation and processing
- `security/` — JWT filter and service (jjwt, HMAC-SHA256, 1h expiry)
- `shared/` — `AbstractErrorHandlerController` base class and `ErrorResponse` record used across all controllers
- `configuration/` — Security, cache (Redis), and core bean configuration

**Key patterns:**
- **DAOs use manual Hibernate Criteria API** (not Spring Data repositories). `EntityManager` + `CriteriaBuilder` + generated JPA metamodel classes (e.g. `User_`).
- **Strategy pattern** for transactions: `TransactionCreatorStrategy` / `TransactionProcessorStrategy` interfaces, with `Purchase*` and `Sell*` implementations. Creators block money (purchase) or stock (sell, recorded per position in `public__transaction_stock_block`); processors apply one fill (`processFill`) or cancel (`cancelTransaction`). Callers lock the wallet first.
- **Order lifecycle:** `POST create` only creates the transaction (`NEW`) and blocks funds. Fills come from `transaction/matching/`: a `TradeFeed` publishes real trades to `TradeBuffer`, `TransactionMatchingJob` (every `transaction.matching.interval-ms`) matches them against the in-memory `OrderBookRegistry` (price-time priority, separate purchase/sell volume pools of `volume * transaction.matching.participation-rate`), and `TransactionFillService` saves the fills, one DB transaction per wallet, skipping transactions that are no longer open. Order books are loaded from the DB on startup and kept in sync by `TransactionOpenedEvent` / `TransactionClosedEvent` (after commit). Modify is cancel-and-replace.
- **Trade feed:** `FinnhubWebSocketTradeFeed` (`transaction/matching/feed/finnhub/`) connects to `wss://ws.finnhub.io`, subscribes only tickers with open transactions, resubscribes after reconnect (exponential backoff) and publishes only normal trades: volume > 0 and every condition code in `finnhub.websocket.allowed-trade-conditions` (default `1,7,8`; trades without conditions are rejected unless `accept-trades-without-conditions`). Counter `finnhub.websocket.trades{result}` shows accepted/rejected trades. `finnhub.websocket.enabled=false` switches to `NoOpTradeFeed`. Finnhub allows one websocket connection per API key and order books are in memory, so only **one backend instance** may run (k8s uses `replicas: 1` + `Recreate`).- **Validators** are singleton enums: `TransactionRequestDTOValidator.I`, `UserRegisterDTOValidator.I`.
- **`UserContextProvider.I`** — singleton enum to get the current authenticated `User` from Spring Security context.
- Controllers extend domain-specific error handler controllers (e.g. `StockErrorHandlerController`), which all extend `AbstractErrorHandlerController`. Cross-domain handlers live in the base class; a domain that needs a different status overrides the base handler method with the same name (a second `@ExceptionHandler` for the same exception type is ambiguous in Spring).
- **No Finnhub HTTP calls inside DB transactions.** Fetch stock data first, then pass it to the `@Transactional` method (e.g. `TransactionService` → `TransactionExecutionService`, `EndOfDayUserWalletsUpdateJob` → `UserWalletService.updateWalletAtTheEndOfTheDay(walletId, stockPrices)`). Read-only flows read a snapshot in a short transaction and compute in memory (`UserWalletDetailsService`). The transactional part lives in a separate bean because `@Transactional` self-invocation bypasses the proxy.
- **Wallet operations lock the wallet row** (`PESSIMISTIC_WRITE` via `UserWalletService.lockUserWallet()` / `findByIdForUpdate`) to serialize concurrent trades and the end-of-day update.
- The `User` entity directly implements `UserDetails`.

**Database:** PostgreSQL 17, schema `stock-exchange-app`, migrations via Flyway (`db/migration/V*__*.sql`). Tables: `public__user`, `public__user_wallet`, `public__user_stock_position`, `public__transaction`, `public__transaction_execution`, `public__transaction_stock_block`, `public__user_favourite_stock`. Hibernate `ddl-auto: none`.

**Caching (Redis):** Two caches — `stockSearchCache` (1d TTL), `stockDataCache` (15s TTL).

**Spring profiles:**
- `local` — connects to `localhost:5432`, no Redis host override, CORS allows `http://localhost:5173`
- `docker` — connects to `stock-exchange-app-database:5432`, Redis at `stock-exchange-app-redis`, CORS allows `http://localhost:5173`

**API structure:** Public endpoints at `/api/public/**`, authenticated endpoints at `/api/user/**` and `/api/transaction/**`. Swagger UI at `/swagger-ui/index.html`.

### Frontend (`frontend/src/`)

- **`page/`** — one directory per route, each with a `.tsx` component and `styles.module.css`
- **`component/`** — reusable UI components with co-located CSS Modules
- **`dto/`** — TypeScript interfaces for request/response shapes
- **`util/`** — service layer organized by domain (user, stocklistings, transaction); each domain has a `*Service.ts` (axios calls) and a `*ServiceUrlProvider.ts` (URL builders)

**Auth:** the login response (`AuthenticationResponseDTO`) returns the JWT, the stored username and the token expiration date; all three are kept in `sessionStorage` via `AuthUtil.ts`, and `isUserAuthenticated()` checks the expiration date (the frontend does not decode the JWT). Protected routes are nested under the `RequireAuth` layout route in `App.tsx`, which redirects to login. Authenticated API calls go through `util/AuthenticatedApiClient.ts`, which adds the `Authorization` header and logs out + redirects to login on 401/403. Public calls use plain `axios`.

**API base URL** built from Vite env vars in `UrlProvider.ts`: `VITE_API_SERVICE_SCHEME`, `VITE_API_SERVICE_HOST`, `VITE_API_SERVICE_PORT` (defaults: `http`, `localhost`, `8080`).

**No frontend tests** — there is no test runner configured.

## Commit Convention

Prefix commits with the affected area in brackets:
- `[BACKEND]` for backend changes
- `[FRONTEND]` for frontend changes
