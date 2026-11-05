package com.s1gawron.stockexchange.transaction.controller;

import com.s1gawron.stockexchange.transaction.dto.TransactionModifyRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionResultDTO;
import com.s1gawron.stockexchange.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/transaction/v1")
public class TransactionController extends TransactionErrorHandlerController {

    private final TransactionService transactionService;

    public TransactionController(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("create")
    public ResponseEntity<TransactionResultDTO> createTransaction(@RequestBody final TransactionRequestDTO transactionRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.validateAndCreateTransaction(transactionRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResultDTO>> getTransactions() {
        return ResponseEntity.ok(transactionService.getTransactions());
    }

    @GetMapping("{transactionId}")
    public ResponseEntity<TransactionResultDTO> getTransaction(@PathVariable final long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @PostMapping("{transactionId}/cancel")
    public ResponseEntity<TransactionResultDTO> cancelTransaction(@PathVariable final long transactionId) {
        return ResponseEntity.ok(transactionService.cancelTransaction(transactionId));
    }

    /**
     * Cancels the transaction and creates a new one with the new price limit and quantity. Returns the new transaction.
     */
    @PutMapping("{transactionId}")
    public ResponseEntity<TransactionResultDTO> modifyTransaction(@PathVariable final long transactionId,
        @RequestBody final TransactionModifyRequestDTO transactionModifyRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.validateAndModifyTransaction(transactionId, transactionModifyRequestDTO));
    }
}
