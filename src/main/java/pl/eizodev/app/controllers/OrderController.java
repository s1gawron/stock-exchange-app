package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.dtos.TransactionDTO;
import pl.eizodev.app.offlineuser.OfflineStockTransaction;
import pl.eizodev.app.services.models.TransactionResult;

@RestController
@RequestMapping("order")
@AllArgsConstructor
class OrderController {

    private final OfflineStockTransaction offlineStockTransaction;

    @PostMapping("perform")
    public TransactionResult processOrder(@RequestBody final TransactionDTO transactionDTO) {
        return offlineStockTransaction.performTransaction(transactionDTO);
    }
}
