#!/usr/bin/env bash
# Checks which Finnhub endpoints our plan allows. Reads FINNHUB_API_KEY from the repo-root .env.
# Prints HTTP status + a short body preview per endpoint. Never prints the key.
# Uses 7 API calls (free tier: ~60/min).
set -euo pipefail
cd "$(dirname "$0")/.."

KEY=$(grep -E '^FINNHUB_API_KEY=' .env 2>/dev/null | head -1 | cut -d= -f2- || true)
if [ -z "${KEY:-}" ]; then
  echo "FINNHUB_API_KEY is not set in .env" >&2
  exit 1
fi

now=$(date +%s)
week_ago=$((now - 7 * 24 * 3600))
endpoints=(
  "/quote?symbol=AAPL"
  "/stock/market-status?exchange=US"
  "/stock/market-holiday?exchange=US"
  "/stock/profile2?symbol=AAPL"
  "/search?q=apple"
  "/stock/candle?symbol=AAPL&resolution=D&from=${week_ago}&to=${now}"
  "/stock/split?symbol=AAPL&from=2020-01-01&to=2026-12-31"
)

for ep in "${endpoints[@]}"; do
  body=$(curl -s -w $'\n%{http_code}' -H "X-Finnhub-Token: ${KEY}" "https://finnhub.io/api/v1${ep}")
  code=${body##*$'\n'}
  preview=$(printf '%s' "${body%$'\n'*}" | tr -d '\n' | cut -c1-110)
  printf '%-4s %-40s %s\n' "$code" "${ep%%\?*}" "$preview"
done
