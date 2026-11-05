package com.s1gawron.stockexchange.user.model;

import java.math.BigDecimal;

/**
 * Quantity blocked on one position by a sell transaction.
 */
public record UserStockBlock(UserStockPosition position, BigDecimal quantity) {

}
