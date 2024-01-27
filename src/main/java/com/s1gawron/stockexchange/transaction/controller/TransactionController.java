package com.s1gawron.stockexchange.transaction.controller;

import com.s1gawron.stockexchange.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;

@RestController
@RequestMapping("transaction")
public class TransactionController extends TransactionErrorHandlerController {

    private final TransactionService transactionService;

    public TransactionController(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("create")
    public ResponseEntity createTransaction(@RequestBody final TransactionRequestDTO transactionRequestDTO) {
        transactionService.createTransaction(transactionRequestDTO);
        return ResponseEntity.ok().build();
    }
}
