-- Phase 1 core schema: users, accounts, instruments, orders, fills, ledger, positions.
-- Money: cash amounts NUMERIC(19,2), prices NUMERIC(19,4), quantities NUMERIC(19,6).

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX users_email_lower_uq ON users (lower(email));

CREATE TABLE accounts (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    currency     CHAR(3)       NOT NULL DEFAULT 'USD',
    cash_balance NUMERIC(19,2) NOT NULL CHECK (cash_balance >= 0),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE instruments (
    symbol         VARCHAR(32)  PRIMARY KEY,
    display_symbol VARCHAR(32)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    type           VARCHAR(64)  NOT NULL,
    currency       CHAR(3),
    mic            VARCHAR(16),
    figi           VARCHAR(32),
    exchange       VARCHAR(128),
    logo_url       VARCHAR(512),
    web_url        VARCHAR(512),
    industry       VARCHAR(128),
    profile_loaded_at TIMESTAMPTZ,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX instruments_name_trgm ON instruments USING gin (lower(name) gin_trgm_ops);

CREATE TABLE orders (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id      BIGINT        NOT NULL REFERENCES accounts (id),
    symbol          VARCHAR(32)   NOT NULL REFERENCES instruments (symbol),
    side            VARCHAR(4)    NOT NULL CHECK (side IN ('BUY', 'SELL')),
    type            VARCHAR(8)    NOT NULL CHECK (type IN ('MARKET')),
    qty             NUMERIC(19,6) NOT NULL CHECK (qty > 0),
    status          VARCHAR(16)   NOT NULL CHECK (status IN ('FILLED', 'REJECTED')),
    reject_reason   VARCHAR(32),
    idempotency_key VARCHAR(64)   NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT orders_idempotency_uq UNIQUE (account_id, idempotency_key)
);
CREATE INDEX orders_account_created_idx ON orders (account_id, created_at DESC);

CREATE TABLE fills (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT        NOT NULL UNIQUE REFERENCES orders (id),
    qty          NUMERIC(19,6) NOT NULL CHECK (qty > 0),
    price        NUMERIC(19,4) NOT NULL CHECK (price > 0),
    gross_amount NUMERIC(19,2) NOT NULL,
    fee          NUMERIC(19,2) NOT NULL DEFAULT 0,
    realized_pnl NUMERIC(19,2),               -- set for SELL fills only
    quote_time   TIMESTAMPTZ   NOT NULL,       -- timestamp of the quote used for the fill
    executed_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Append-only. Source of truth for cash and holdings.
CREATE TABLE ledger_entries (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id  BIGINT        NOT NULL REFERENCES accounts (id),
    type        VARCHAR(16)   NOT NULL CHECK (type IN ('DEPOSIT', 'BUY', 'SELL', 'FEE')),
    symbol      VARCHAR(32)   REFERENCES instruments (symbol),
    cash_amount NUMERIC(19,2) NOT NULL,   -- signed: + into account, - out of account
    qty_amount  NUMERIC(19,6) NOT NULL DEFAULT 0, -- signed share change
    order_id    BIGINT        REFERENCES orders (id),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX ledger_account_idx ON ledger_entries (account_id, id);

-- Cached view of holdings; must reconcile with ledger_entries.
CREATE TABLE positions (
    account_id BIGINT        NOT NULL REFERENCES accounts (id),
    symbol     VARCHAR(32)   NOT NULL REFERENCES instruments (symbol),
    qty        NUMERIC(19,6) NOT NULL CHECK (qty > 0),
    cost_basis NUMERIC(19,2) NOT NULL CHECK (cost_basis >= 0),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, symbol)
);
