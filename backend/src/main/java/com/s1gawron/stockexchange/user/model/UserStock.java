package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.shared.MoneyRounding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * All positions of one ticker in one wallet. Not persisted - the quantities and the average purchase price are computed from the positions.
 */
public class UserStock {

    private static final Comparator<UserStockPosition> FIFO_ORDER = Comparator.comparing(UserStockPosition::getOpenDate)
        .thenComparing(UserStockPosition::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final String ticker;

    private final List<UserStockPosition> positions;

    private UserStock(final String ticker, final List<UserStockPosition> positions) {
        this.ticker = ticker;
        this.positions = positions.stream().sorted(FIFO_ORDER).toList();
    }

    public static List<UserStock> fromPositions(final List<UserStockPosition> positions) {
        final Map<String, List<UserStockPosition>> positionsByTicker = new LinkedHashMap<>();

        for (final UserStockPosition position : positions) {
            positionsByTicker.computeIfAbsent(position.getTicker(), ticker -> new ArrayList<>()).add(position);
        }

        return positionsByTicker.entrySet().stream()
            .map(entry -> new UserStock(entry.getKey(), entry.getValue()))
            .toList();
    }

    public static Optional<UserStock> fromTickerPositions(final List<UserStockPosition> positions) {
        return fromPositions(positions).stream().findFirst();
    }

    public String getTicker() {
        return ticker;
    }

    public String getCurrency() {
        return positions.get(0).getCurrency();
    }

    public List<UserStockPosition> getPositions() {
        return positions;
    }

    public Optional<UserStockPosition> getPosition(final long positionId) {
        return positions.stream()
            .filter(position -> position.getId() != null && position.getId() == positionId)
            .findFirst();
    }

    public BigDecimal getQuantityAvailable() {
        return sum(UserStockPosition::getQuantityAvailable);
    }

    public BigDecimal getQuantityBlocked() {
        return sum(UserStockPosition::getQuantityBlocked);
    }

    public BigDecimal getAllStockQuantity() {
        return sum(UserStockPosition::getAllStockQuantity);
    }

    public BigDecimal getAveragePurchasePrice() {
        final BigDecimal allStockQuantity = getAllStockQuantity();

        if (allStockQuantity.signum() == 0) {
            return MoneyRounding.I.money(BigDecimal.ZERO);
        }

        final BigDecimal allStockValue = sum(position -> position.getOpenPrice().multiply(position.getAllStockQuantity()));
        return allStockValue.divide(allStockQuantity, MoneyRounding.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Blocks the quantity on the oldest positions first and returns the positions that changed.
     */
    public List<UserStockPosition> blockStock(final BigDecimal transactionQuantity) {
        final List<UserStockPosition> changedPositions = new ArrayList<>();
        BigDecimal quantityToBlock = transactionQuantity;

        for (final UserStockPosition position : positions) {
            if (quantityToBlock.signum() == 0) {
                break;
            }

            final BigDecimal positionQuantityToBlock = quantityToBlock.min(position.getQuantityAvailable());

            if (positionQuantityToBlock.signum() > 0) {
                position.blockStock(positionQuantityToBlock);
                changedPositions.add(position);
                quantityToBlock = quantityToBlock.subtract(positionQuantityToBlock);
            }
        }

        if (quantityToBlock.signum() > 0) {
            throw new IllegalStateException("Cannot block more stock than available!");
        }

        return changedPositions;
    }

    /**
     * Releases the blocked quantity from the oldest positions first and returns the positions that changed.
     */
    public List<UserStockPosition> releaseStock(final BigDecimal transactionQuantity) {
        final List<UserStockPosition> changedPositions = new ArrayList<>();
        BigDecimal quantityToRelease = transactionQuantity;

        for (final UserStockPosition position : positions) {
            if (quantityToRelease.signum() == 0) {
                break;
            }

            final BigDecimal positionQuantityToRelease = quantityToRelease.min(position.getQuantityBlocked());

            if (positionQuantityToRelease.signum() > 0) {
                position.releaseStock(positionQuantityToRelease);
                changedPositions.add(position);
                quantityToRelease = quantityToRelease.subtract(positionQuantityToRelease);
            }
        }

        if (quantityToRelease.signum() > 0) {
            throw new IllegalStateException("Cannot release more stock than blocked!");
        }

        return changedPositions;
    }

    private BigDecimal sum(final Function<UserStockPosition, BigDecimal> mapper) {
        return positions.stream()
            .map(mapper)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
