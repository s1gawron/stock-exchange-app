package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.dto.TransactionDTO;
import pl.eizodev.app.offlineuser.OfflineStockTransaction;

@RestController
@RequestMapping("order")
@AllArgsConstructor
class OrderController {
    private final OfflineStockTransaction offlineStockTransaction;

    @PostMapping("perform")
    public ResponseEntity<?> processOrder(@RequestBody final TransactionDTO transactionDTO, final BindingResult result) {
        final boolean userCantPerformTransaction = offlineStockTransaction.transactionHasErrors(transactionDTO, result);

        if (userCantPerformTransaction) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        } else {
            offlineStockTransaction.performTransaction(transactionDTO);
            return ResponseEntity.ok().body(transactionDTO);
        }
    }
}
