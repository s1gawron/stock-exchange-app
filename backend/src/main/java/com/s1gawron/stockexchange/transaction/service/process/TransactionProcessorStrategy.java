package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.transaction.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Changes the wallet and the stock positions for one transaction type. The caller must lock the wallet of the transaction and check that the
 * transaction is still open before it calls these methods.
 */
public interface TransactionProcessorStrategy {

    /**
     * Fills the given quantity of the transaction at the given price.
     */
    void processFill(Transaction transaction, BigDecimal quantity, BigDecimal price, LocalDateTime sourceTradeDate);

    /**
     * Cancels the transaction and releases the money or the stock that is still blocked for it.
     */
    void cancelTransaction(Transaction transaction);

}
