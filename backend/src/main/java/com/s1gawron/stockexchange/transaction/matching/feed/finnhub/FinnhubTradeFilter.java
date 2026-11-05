package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Decides which Finnhub trades can fill transactions. Only normal trades are used: every trade condition code must be in the allowed set. By
 * default these are the US CTA/UTP codes 1 (Regular), 7 (Automatic Execution) and 8 (Intermarket Sweep Order). All special trades are rejected,
 * e.g. odd lots (12), extended hours (24, 25), opening and closing prints (18, 19, 35, 36) and out of sequence trades (21, 30).
 * <p>
 * Condition codes: <a href="https://docs.google.com/spreadsheets/d/1PUxiSWPHSODbaTaoL2Vef6DgU-yFtlRGZf19oBb9Hp0">Finnhub trade conditions</a>
 */
public class FinnhubTradeFilter {

    public enum Result {
        ACCEPTED,
        INVALID,
        NO_VOLUME,
        NO_CONDITIONS,
        SPECIAL_CONDITION
    }

    private final Set<String> allowedConditions;

    private final boolean acceptNoConditions;

    public FinnhubTradeFilter(final Set<String> allowedConditions, final boolean acceptNoConditions) {
        this.allowedConditions = Set.copyOf(allowedConditions);
        this.acceptNoConditions = acceptNoConditions;
    }

    public Result check(final FinnhubTradeDTO trade) {
        if (trade.symbol() == null || trade.price() == null || trade.price().signum() <= 0 || trade.timestamp() <= 0) {
            return Result.INVALID;
        }

        // Finnhub sends price updates without trades with volume 0.
        if (trade.volume() == null || trade.volume().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.NO_VOLUME;
        }

        final List<String> conditions = trade.conditions();

        if (conditions == null || conditions.isEmpty()) {
            return acceptNoConditions ? Result.ACCEPTED : Result.NO_CONDITIONS;
        }

        return allowedConditions.containsAll(conditions) ? Result.ACCEPTED : Result.SPECIAL_CONDITION;
    }
}
