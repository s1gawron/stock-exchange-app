package pl.eizodev.app.transaction.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.transaction.TransactionService;
import pl.eizodev.app.transaction.dto.TransactionDTO;
import pl.eizodev.app.transaction.dto.TransactionResultDTO;

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
