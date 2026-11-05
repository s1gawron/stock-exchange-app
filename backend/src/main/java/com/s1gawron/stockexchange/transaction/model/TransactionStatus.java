package com.s1gawron.stockexchange.transaction.model;

public enum TransactionStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED;

    public boolean isOpen() {
        return this == NEW || this == PARTIALLY_FILLED;
    }
}
