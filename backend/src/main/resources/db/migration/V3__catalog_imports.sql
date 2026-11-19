-- Tracks catalog imports so startup can re-import after a provider switch (fake <-> finnhub) or a long downtime.
CREATE TABLE catalog_imports (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider     VARCHAR(32) NOT NULL,
    active_count INT         NOT NULL,
    imported_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
