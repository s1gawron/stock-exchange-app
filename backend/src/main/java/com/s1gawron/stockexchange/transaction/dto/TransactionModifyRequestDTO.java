package com.s1gawron.stockexchange.transaction.dto;

import java.math.BigDecimal;

/**
 * New price limit and new quantity of an open transaction. For a partially filled transaction, the quantity is the quantity that is still to be
 * filled, because the filled part stays in the old transaction.
 */
public record TransactionModifyRequestDTO(BigDecimal price, BigDecimal quantity) {

}
