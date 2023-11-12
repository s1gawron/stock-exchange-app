package com.s1gawron.stockexchange.transaction;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.s1gawron.stockexchange.transaction.dto.TransactionDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionResultDTO;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Transactional
public class TransactionService {

    public TransactionResultDTO performTransaction(final TransactionDTO transactionDTO) {
        if (transactionDTO.transactionType() == TransactionType.PURCHASE) {
            return stockPurchase(transactionDTO);
        } else {
            return stockSell(transactionDTO);
        }
    }

    private TransactionResultDTO stockPurchase(final TransactionDTO transactionDTO) {
        //logic deleted so as not to create conflicts with new logic WORK IN PROGRESS
        return new TransactionResultDTO(1L, "test", TransactionType.SELL, "AAPL", 1, BigDecimal.valueOf(1.23), BigDecimal.valueOf(1.23));
    }

    private TransactionResultDTO stockSell(final TransactionDTO transactionDTO) {
        //logic deleted so as not to create conflicts with new logic WORK IN PROGRESS
        return new TransactionResultDTO(1L, "test", TransactionType.SELL, "AAPL", 1, BigDecimal.valueOf(1.23), BigDecimal.valueOf(1.23));
    }
}