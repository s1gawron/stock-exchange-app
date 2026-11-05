package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionResultDTO;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotOpenException;
import com.s1gawron.stockexchange.transaction.exception.WalletCurrencyMismatchException;
import com.s1gawron.stockexchange.transaction.matching.OrderBookEntry;
import com.s1gawron.stockexchange.transaction.matching.event.TransactionClosedEvent;
import com.s1gawron.stockexchange.transaction.matching.event.TransactionOpenedEvent;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionExecutionService {

    private final PurchaseTransactionCreator purchaseTransactionCreator;

    private final SellTransactionCreator sellTransactionCreator;

    private final PurchaseTransactionProcessor purchaseTransactionProcessor;

    private final SellTransactionProcessor sellTransactionProcessor;

    private final TransactionDAO transactionDAO;

    private final UserWalletService userWalletService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public TransactionExecutionService(final PurchaseTransactionCreator purchaseTransactionCreator, final SellTransactionCreator sellTransactionCreator,
        final PurchaseTransactionProcessor purchaseTransactionProcessor, final SellTransactionProcessor sellTransactionProcessor,
        final TransactionDAO transactionDAO, final UserWalletService userWalletService, final ApplicationEventPublisher applicationEventPublisher) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
        this.sellTransactionCreator = sellTransactionCreator;
        this.purchaseTransactionProcessor = purchaseTransactionProcessor;
        this.sellTransactionProcessor = sellTransactionProcessor;
        this.transactionDAO = transactionDAO;
        this.userWalletService = userWalletService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Creates the transaction with status NEW and blocks the money or the stock for it. The transaction is filled later by the matching job.
     */
    @Transactional
    public TransactionResultDTO createTransaction(final TransactionRequestDTO transactionRequestDTO, final String stockCurrency) {
        final UserWallet userWallet = userWalletService.lockUserWallet();

        if (!userWallet.getCurrency().equals(stockCurrency)) {
            throw WalletCurrencyMismatchException.create(transactionRequestDTO.stockTicker(), stockCurrency, userWallet.getCurrency());
        }

        final Transaction transaction = openTransaction(transactionRequestDTO, stockCurrency);
        return TransactionResultDTO.from(transaction, List.of());
    }

    @Transactional
    public TransactionResultDTO cancelTransaction(final long transactionId) {
        final UserWallet userWallet = userWalletService.lockUserWallet();
        final Transaction transaction = getOpenTransactionOfWallet(userWallet, transactionId);

        closeTransaction(transaction);

        return TransactionResultDTO.from(transaction, getExecutions(transaction));
    }

    /**
     * Cancels the transaction and creates a new one with the given price limit and quantity (cancel and replace). The fills of the canceled
     * transaction stay in it.
     */
    @Transactional
    public TransactionResultDTO modifyTransaction(final long transactionId, final BigDecimal price, final BigDecimal quantity) {
        final UserWallet userWallet = userWalletService.lockUserWallet();
        final Transaction transaction = getOpenTransactionOfWallet(userWallet, transactionId);

        closeTransaction(transaction);

        final TransactionRequestDTO newTransactionRequestDTO = new TransactionRequestDTO(transaction.getTransactionType(),
            transaction.getTransactionPosition().getStockTicker(), price, quantity, transaction.getSellPositionId().orElse(null));
        final Transaction newTransaction = openTransaction(newTransactionRequestDTO, transaction.getTransactionPosition().getStockCurrency());

        return TransactionResultDTO.from(newTransaction, List.of());
    }

    @Transactional(readOnly = true)
    public List<TransactionResultDTO> getTransactions() {
        final UserWallet userWallet = userWalletService.getUserWallet();
        final List<Transaction> transactions = transactionDAO.getTransactionsByWalletId(userWallet.getId());
        final Map<Long, List<TransactionExecution>> executionsByTransactionId = transactionDAO.getTransactionExecutions(
                transactions.stream().map(Transaction::getId).toList()).stream()
            .collect(Collectors.groupingBy(TransactionExecution::getTransactionId));

        return transactions.stream()
            .map(transaction -> TransactionResultDTO.from(transaction, executionsByTransactionId.getOrDefault(transaction.getId(), List.of())))
            .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResultDTO getTransaction(final long transactionId) {
        final UserWallet userWallet = userWalletService.getUserWallet();
        final Transaction transaction = getTransactionOfWallet(userWallet, transactionId);

        return TransactionResultDTO.from(transaction, getExecutions(transaction));
    }

    private Transaction openTransaction(final TransactionRequestDTO transactionRequestDTO, final String stockCurrency) {
        final TransactionCreatorStrategy strategy = transactionRequestDTO.type().isPurchase() ? purchaseTransactionCreator : sellTransactionCreator;

        if (!strategy.canCreateTransaction(transactionRequestDTO)) {
            throw new IllegalStateException("Method should not execute here, because canCreateTransaction() throws exception if cannot be created!");
        }

        final long transactionId = strategy.createTransaction(transactionRequestDTO, stockCurrency);
        final Transaction transaction = transactionDAO.getTransactionById(transactionId)
            .orElseThrow(() -> TransactionNotFoundException.create(transactionId));

        applicationEventPublisher.publishEvent(new TransactionOpenedEvent(OrderBookEntry.from(transaction)));

        return transaction;
    }

    private void closeTransaction(final Transaction transaction) {
        final TransactionProcessorStrategy strategy = transaction.getTransactionType().isPurchase() ? purchaseTransactionProcessor : sellTransactionProcessor;

        strategy.cancelTransaction(transaction);
        applicationEventPublisher.publishEvent(new TransactionClosedEvent(transaction.getId()));
    }

    private Transaction getOpenTransactionOfWallet(final UserWallet userWallet, final long transactionId) {
        final Transaction transaction = getTransactionOfWallet(userWallet, transactionId);

        if (!transaction.isOpen()) {
            throw TransactionNotOpenException.create(transaction.getId(), transaction.getTransactionStatus());
        }

        return transaction;
    }

    /**
     * A transaction of another wallet is reported as not found, so its id does not tell that it exists.
     */
    private Transaction getTransactionOfWallet(final UserWallet userWallet, final long transactionId) {
        return transactionDAO.getTransactionById(transactionId)
            .filter(transaction -> transaction.getWalletId().equals(userWallet.getId()))
            .orElseThrow(() -> TransactionNotFoundException.create(transactionId));
    }

    private List<TransactionExecution> getExecutions(final Transaction transaction) {
        return transactionDAO.getTransactionExecutions(List.of(transaction.getId()));
    }

}
