-- Phase 3: daily account snapshots, benchmark closes, competitions.

-- One row per account per US trading day, taken after the close.
CREATE TABLE account_snapshots (
    account_id      BIGINT        NOT NULL REFERENCES accounts (id),
    day             DATE          NOT NULL,   -- New York trading day
    cash            NUMERIC(19,2) NOT NULL,
    market_value    NUMERIC(19,2) NOT NULL,
    equity          NUMERIC(19,2) NOT NULL,
    deposits        NUMERIC(19,2) NOT NULL,
    prices_complete BOOLEAN       NOT NULL,   -- false: some holding had no price and was valued at cost
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, day)
);
CREATE INDEX account_snapshots_day_idx ON account_snapshots (day);

-- Daily close of the benchmark (SPY by default).
CREATE TABLE benchmark_closes (
    symbol VARCHAR(32)   NOT NULL,
    day    DATE          NOT NULL,
    close  NUMERIC(19,4) NOT NULL CHECK (close > 0),
    PRIMARY KEY (symbol, day)
);

CREATE TABLE competitions (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(16)  NOT NULL UNIQUE,  -- join code shared by the creator
    creator_user_id BIGINT       NOT NULL REFERENCES users (id),
    starts_on       DATE         NOT NULL,         -- first trading day counted
    ends_on         DATE         NOT NULL,         -- last trading day counted
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (ends_on >= starts_on)
);

CREATE TABLE competition_members (
    competition_id BIGINT      NOT NULL REFERENCES competitions (id) ON DELETE CASCADE,
    user_id        BIGINT      NOT NULL REFERENCES users (id),
    joined_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (competition_id, user_id)
);
CREATE INDEX competition_members_user_idx ON competition_members (user_id);
