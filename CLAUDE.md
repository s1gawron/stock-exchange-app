# Stock Simulator: rules for agents

Paper-trading app. Backend: Spring Boot 4.1, Java 21, Maven, plain JDBC (`JdbcClient`), PostgreSQL, Redis.
Frontend: React in `frontend/`. Read `README.md` and `docs/PLAN.md` (Decision log) first.
`ArchitectureTest` enforces the backend rules below. A rule break fails the build.

## Backend architecture

Package by feature: `com.stocksim.<module>`. Modules: `account`, `auth`, `common`, `competition`, `instrument`,
`ledger`, `marketdata`, `performance`, `portfolio`, `realtime`, `trading`, `watchlist`.

- Request flow: `Controller -> Service -> Repository`.
- Controllers: no SQL, no business logic. They map HTTP to one service call.
- SQL only in classes named `*Repository`, annotated `@Repository`. No `JdbcClient`/`JdbcTemplate` anywhere else.
- Repositories are package-private. Other modules never see them.
- Other modules use a module only through its public API: a small public service (or query class). Keep
  everything else package-private. No cyclic dependencies between modules.
- If module A needs data from module B and B already depends on A, declare a small interface in A and
  implement it in B (example: `portfolio.RealizedPnlSource`, implemented by `trading.TradingRealizedPnl`).
  Ask the owner before adding such an interface.
- Plain JDBC. Do not add JPA, MapStruct or service interfaces without asking the owner.

### Entities and DTOs

There is no JPA. Repository results are plain immutable records. Nothing is written back automatically.
The rules are about decoupling.

- Entity records (table-row types returned by repositories) are named `*Row` and are package-private.
  Examples: `AccountRow`, `PositionRow`, `InstrumentRow`, `UserRow`, `OrderRepository.OrderRow`.
- Controllers and other modules get DTOs: immutable records with only the fields they need, mapped in the
  service. Examples: `AccountBalance`, `PositionHolding`, `InstrumentView`, `InstrumentSummary`.
- Response types that are already DTOs keep their JSON field names (`OrderView`, `PortfolioService.Portfolio`).
  If you replace an entity in a response, the new DTO must give exactly the same JSON.
- Business decisions on money (cash, reservations, positions) are made inside the locking transaction on freshly
  read data, never on an entity or DTO read earlier (see `OrderExecutor`).

### Table ownership

Each table is read and written only by the repository of the owning module.

| Table | Module |
|---|---|
| `users` | auth |
| `accounts` | account |
| `ledger_entries` | ledger |
| `orders`, `fills` | trading |
| `positions` | portfolio |
| `instruments`, `catalog_imports` | instrument |
| `watchlist_items` | watchlist |
| `account_snapshots`, `benchmark_closes` | performance |
| `competitions`, `competition_members` | competition |

Documented exceptions (read-only reporting queries that JOIN other modules' tables in one SQL query, for
performance). They are listed in `ArchitectureTest.REPORTING_EXCEPTIONS`:

- `performance/LeaderboardRepository`: reads `accounts`, `users`.
- `competition/CompetitionStandingsRepository`: reads `users`, `accounts`, `account_snapshots`, `ledger_entries`.

Do not add an exception without asking the owner.

### Money rules

- `trading/OrderExecutor` is the ONLY place that changes cash, reservations and positions.
  The architecture test checks the callers of `AccountService.lock/updateBalances`,
  `PositionService.save/addReserved` and `LedgerService.append`.
- Keep its transactions and lock order: account row first, then order row.
- The ledger is append-only (no UPDATE or DELETE on `ledger_entries`).
- Do not put `@Transactional` on the module API methods that `OrderExecutor` calls. They must join its transaction.

### Things that must not change by accident

REST endpoints, JSON field names, status codes, the WebSocket message format on `/ws`, configuration properties,
scheduling and timing rules. Do not add or edit Flyway migrations (`db/migration`) unless the owner asks.

## How to run tests

Java 21 is required (the default JDK may be 17). Docker must run (Testcontainers starts Postgres and Redis).

```
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw verify                          # all tests
./mvnw test -Dtest=ArchitectureTest    # architecture rules only (no Docker needed)
```

Frontend: `cd frontend && npm run lint && npm run build`.

Do not weaken or delete test assertions to make a change pass. If a class moves, move its test and keep what it checks.

## Smoke test of the full app

Never use the local dev database (Postgres 5433, Redis 6380, backend 8080). Use a throwaway Compose project:

```
DB_PORT=5434 REDIS_PORT=6381 docker compose -p stocksim-smoke up -d
cd backend && SERVER_PORT=8081 DB_PORT=5434 REDIS_PORT=6381 MARKET_DATA_PROVIDER=fake ./mvnw spring-boot:run
docker compose -p stocksim-smoke down -v     # clean up afterwards
```

## Git

Commit to `main` directly, small steps, one clear message each. `git add` every new file. Never commit `.env` or secrets.
