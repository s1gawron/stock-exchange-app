-- Phase 2: limit/stop orders with DAY/GTC, cash/share reservations, watchlists.

ALTER TABLE orders DROP CONSTRAINT orders_type_check;
ALTER TABLE orders ADD CONSTRAINT orders_type_check CHECK (type IN ('MARKET', 'LIMIT', 'STOP'));

ALTER TABLE orders DROP CONSTRAINT orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check
    CHECK (status IN ('OPEN', 'FILLED', 'REJECTED', 'CANCELLED', 'EXPIRED'));

ALTER TABLE orders
    ADD COLUMN limit_price   NUMERIC(19,4) CHECK (limit_price > 0),
    ADD COLUMN stop_price    NUMERIC(19,4) CHECK (stop_price > 0),
    ADD COLUMN time_in_force VARCHAR(3)    CHECK (time_in_force IN ('DAY', 'GTC')),
    -- What this order holds while OPEN; both go back to 0 when it leaves OPEN.
    ADD COLUMN reserved_cash NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (reserved_cash >= 0),
    ADD COLUMN reserved_qty  NUMERIC(19,6) NOT NULL DEFAULT 0 CHECK (reserved_qty >= 0),
    ADD COLUMN expires_at    TIMESTAMPTZ,  -- DAY orders: close of their session
    ADD COLUMN updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now();

ALTER TABLE orders ADD CONSTRAINT orders_prices_match_type CHECK (
    (type = 'MARKET' AND limit_price IS NULL AND stop_price IS NULL AND time_in_force IS NULL) OR
    (type = 'LIMIT'  AND limit_price IS NOT NULL AND stop_price IS NULL AND time_in_force IS NOT NULL) OR
    (type = 'STOP'   AND stop_price IS NOT NULL AND limit_price IS NULL AND time_in_force IS NOT NULL));

CREATE INDEX orders_open_symbol_idx ON orders (symbol) WHERE status = 'OPEN';
CREATE INDEX orders_open_expiry_idx ON orders (expires_at) WHERE status = 'OPEN' AND expires_at IS NOT NULL;

-- Reservations held by OPEN orders. Must equal the sum over the account's OPEN orders.
ALTER TABLE accounts ADD COLUMN reserved_cash NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE accounts ADD CONSTRAINT accounts_reserved_cash_check
    CHECK (reserved_cash >= 0 AND reserved_cash <= cash_balance);

ALTER TABLE positions ADD COLUMN reserved_qty NUMERIC(19,6) NOT NULL DEFAULT 0;
ALTER TABLE positions ADD CONSTRAINT positions_reserved_qty_check
    CHECK (reserved_qty >= 0 AND reserved_qty <= qty);

CREATE TABLE watchlist_items (
    user_id    BIGINT      NOT NULL REFERENCES users (id),
    symbol     VARCHAR(32) NOT NULL REFERENCES instruments (symbol),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, symbol)
);
