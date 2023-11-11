package com.s1gawron.stockexchange.transaction.controller;

import com.s1gawron.stockexchange.transaction.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.s1gawron.stockexchange.transaction.dto.TransactionDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionResultDTO;

@RestController
@RequestMapping("transaction")
@AllArgsConstructor
public class TransactionController extends TransactionErrorHandlerController {

    private final TransactionService transactionService;

    @PostMapping("perform")
    public TransactionResultDTO processOrder(@RequestBody final TransactionDTO transactionDTO) {
        return transactionService.performTransaction(transactionDTO);
    }
}
