package pl.eizodev.app.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.dto.TransactionDTO;
import pl.eizodev.app.offlineuser.OfflineStockTransaction;
import pl.eizodev.app.service.model.TransactionResult;

@RestController
@RequestMapping("order")
@AllArgsConstructor
@CrossOrigin
class OrderController extends AbstractErrorHandlerController {

    private final OfflineStockTransaction offlineStockTransaction;

    @PostMapping("perform")
    public TransactionResult processOrder(@RequestBody final TransactionDTO transactionDTO) {
        return offlineStockTransaction.performTransaction(transactionDTO);
    }
}
