package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.InMemoryStockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.impl.InMemoryTransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionResultDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotOpenException;
import com.s1gawron.stockexchange.transaction.exception.WalletCurrencyMismatchException;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.matching.Fill;
import com.s1gawron.stockexchange.transaction.matching.event.TransactionClosedEvent;
import com.s1gawron.stockexchange.transaction.matching.event.TransactionOpenedEvent;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionPosition;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserWalletDAO;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the whole transaction lifecycle (create, fill, cancel, modify) with the real creators, processors and services and in-memory DAOs, so the
 * money and stock accounting is checked from start to end.
 */
class TransactionLifecycleTest {

    private static final Instant NOW = Instant.parse("2026-11-05T15:00:00Z");

    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private static final long USER_ID = 1L;

    private static final String TICKER = "AAPL";

    private InMemoryUserWalletDAO userWalletDAO;

    private InMemoryTransactionDAO transactionDAO;

    private List<Object> publishedEvents;

    private TransactionExecutionService transactionExecutionService;

    private TransactionFillService transactionFillService;

    private PurchaseTransactionProcessor purchaseTransactionProcessor;

    private UserWallet userWallet;

    @BeforeEach
    void setUp() {
        final Authentication authenticationMock = Mockito.mock(Authentication.class);
        final SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(UserCreatorHelper.I.createUser(USER_ID, "testUser"));
        SecurityContextHolder.setContext(securityContextMock);

        userWalletDAO = new InMemoryUserWalletDAO();
        transactionDAO = new InMemoryTransactionDAO();
        publishedEvents = new ArrayList<>();

        final UserWalletService userWalletService = new UserWalletService(userWalletDAO, new InMemoryStockDataProvider(), CLOCK);
        purchaseTransactionProcessor = new PurchaseTransactionProcessor(userWalletService, transactionDAO, CLOCK);
        final SellTransactionProcessor sellTransactionProcessor = new SellTransactionProcessor(userWalletService, transactionDAO, CLOCK);

        transactionExecutionService = new TransactionExecutionService(new PurchaseTransactionCreator(userWalletService, transactionDAO, CLOCK),
            new SellTransactionCreator(userWalletService, transactionDAO, CLOCK), purchaseTransactionProcessor, sellTransactionProcessor, transactionDAO,
            userWalletService, publishedEvents::add);
        transactionFillService = new TransactionFillService(purchaseTransactionProcessor, sellTransactionProcessor, transactionDAO, userWalletService,
            CLOCK);

        userWallet = UserWallet.createUserWallet(USER_ID, new BigDecimal("10000.00"), LocalDateTime.now(CLOCK));
        userWalletDAO.saveUserWallet(userWallet);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreatePurchaseTransactionWithStatusNewAndBlockMoneyWithoutFillingIt() {
        final TransactionResultDTO result = createPurchase("25.00", "10");

        assertEquals(TransactionStatus.NEW, result.transactionStatus());
        assertMoney("0", result.filledQuantity());
        assertMoney("10", result.remainingQuantity());
        assertNull(result.averageFillPrice());
        assertMoney("250", result.balanceBlocked());
        assertTrue(result.executions().isEmpty());
        assertMoney("9750", userWallet.getBalanceAvailable());
        assertMoney("250", userWallet.getBalanceBlocked());
        assertTrue(userWalletDAO.getUserStockPositions(userWallet.getId()).isEmpty());

        final TransactionOpenedEvent event = assertInstanceOf(TransactionOpenedEvent.class, publishedEvents.get(0));
        assertEquals(result.transactionId(), event.entry().getTransactionId());
        assertMoney("10", event.entry().getRemainingQuantity());
    }

    @Test
    void shouldNotCreateTransactionWhenStockCurrencyIsDifferentThanWalletCurrency() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "VOD.L", new BigDecimal("60.00"),
            new BigDecimal("10"));

        assertThrows(WalletCurrencyMismatchException.class, () -> transactionExecutionService.createTransaction(transactionRequestDTO, "GBP"));
        assertTrue(publishedEvents.isEmpty());
    }

    @Test
    void shouldPartiallyAndThenFullyFillPurchaseIntoOnePositionAndReturnUnspentMoney() {
        final long transactionId = createPurchase("25.00", "10").transactionId();

        applyFill(transactionId, "4", "24.00");

        Transaction transaction = getTransaction(transactionId);
        assertEquals(TransactionStatus.PARTIALLY_FILLED, transaction.getTransactionStatus());
        assertMoney("4", transaction.getFilledQuantity());
        assertMoney("24", transaction.getAverageFillPrice().orElseThrow());
        assertMoney("150", transaction.getBalanceBlocked());
        assertMoney("9754", userWallet.getBalanceAvailable());
        assertMoney("150", userWallet.getBalanceBlocked());
        assertNull(transaction.getTransactionProcessDate());

        applyFill(transactionId, "6", "25.00");

        transaction = getTransaction(transactionId);
        assertEquals(TransactionStatus.FILLED, transaction.getTransactionStatus());
        assertMoney("10", transaction.getFilledQuantity());
        assertMoney("24.6", transaction.getAverageFillPrice().orElseThrow());
        assertMoney("0", transaction.getBalanceBlocked());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
        assertMoney("9754", userWallet.getBalanceAvailable());
        assertMoney("0", userWallet.getBalanceBlocked());

        final List<UserStockPosition> positions = userWalletDAO.getUserStockPositions(userWallet.getId());
        assertEquals(1, positions.size());
        assertEquals(transaction.getOpenedPositionId().orElseThrow(), positions.get(0).getId());
        assertMoney("10", positions.get(0).getQuantityAvailable());
        assertMoney("24.6", positions.get(0).getOpenPrice());
        assertEquals(2, transactionDAO.getAllTransactionExecutions().size());
    }

    @Test
    void shouldReleaseAllBlockedMoneyOnLastFillEvenWhenPartialFillsWereRounded() {
        final long transactionId = createPurchase("0.0003", "0.5").transactionId();
        assertMoney("0.0002", userWallet.getBalanceBlocked());

        applyFill(transactionId, "0.25", "0.0003");
        applyFill(transactionId, "0.25", "0.0003");

        assertEquals(TransactionStatus.FILLED, getTransaction(transactionId).getTransactionStatus());
        assertMoney("0", userWallet.getBalanceBlocked());
        assertMoney("9999.9998", userWallet.getBalanceAvailable());
    }

    @Test
    void shouldCancelPartiallyFilledPurchaseAndReleaseRemainingMoney() {
        final long transactionId = createPurchase("25.00", "10").transactionId();
        applyFill(transactionId, "4", "24.00");

        final TransactionResultDTO result = transactionExecutionService.cancelTransaction(transactionId);

        assertEquals(TransactionStatus.CANCELED, result.transactionStatus());
        assertMoney("4", result.filledQuantity());
        assertMoney("0", result.balanceBlocked());
        assertEquals(1, result.executions().size());
        assertEquals(LocalDateTime.now(CLOCK), result.transactionProcessDate());
        assertMoney("9904", userWallet.getBalanceAvailable());
        assertMoney("0", userWallet.getBalanceBlocked());
        assertMoney("4", userWalletDAO.getUserStockPositions(userWallet.getId()).get(0).getQuantityAvailable());

        final TransactionClosedEvent event = assertInstanceOf(TransactionClosedEvent.class, publishedEvents.get(publishedEvents.size() - 1));
        assertEquals(transactionId, event.transactionId());
    }

    @Test
    void shouldNotCancelTransactionThatIsAlreadyFilled() {
        final long transactionId = createPurchase("25.00", "10").transactionId();
        applyFill(transactionId, "10", "25.00");

        assertThrows(TransactionNotOpenException.class, () -> transactionExecutionService.cancelTransaction(transactionId));
    }

    @Test
    void shouldNotFindTransactionOfAnotherWallet() {
        final UserWallet otherUserWallet = UserWallet.createUserWallet(2L, new BigDecimal("10000.00"), LocalDateTime.now(CLOCK));
        userWalletDAO.saveUserWallet(otherUserWallet);
        final Transaction otherUserTransaction = new Transaction(otherUserWallet.getId(), TransactionType.PURCHASE, TransactionStatus.NEW,
            LocalDateTime.now(CLOCK), new TransactionPosition(TICKER, new BigDecimal("25.00"), BigDecimal.ONE, "USD"));
        transactionDAO.saveTransaction(otherUserTransaction);

        assertThrows(TransactionNotFoundException.class, () -> transactionExecutionService.cancelTransaction(otherUserTransaction.getId()));
        assertThrows(TransactionNotFoundException.class, () -> transactionExecutionService.getTransaction(otherUserTransaction.getId()));
        assertTrue(transactionExecutionService.getTransactions().isEmpty());
    }

    @Test
    void shouldSellFromOldestPositionsFirstAndReleaseSoldStockFromThePositionsBlockedByTransaction() {
        final UserStockPosition olderPosition = savePosition("5", "20.00", 1);
        final UserStockPosition newerPosition = savePosition("5", "22.00", 2);

        final long transactionId = createSell("30.00", "7", null).transactionId();

        assertMoney("0", olderPosition.getQuantityAvailable());
        assertMoney("5", olderPosition.getQuantityBlocked());
        assertMoney("3", newerPosition.getQuantityAvailable());
        assertMoney("2", newerPosition.getQuantityBlocked());
        assertEquals(2, transactionDAO.getTransactionStockBlocks(transactionId).size());

        applyFill(transactionId, "6", "31.00");

        assertEquals(TransactionStatus.PARTIALLY_FILLED, getTransaction(transactionId).getTransactionStatus());
        assertTrue(userWalletDAO.findUserStockPositionById(olderPosition.getId()).isEmpty());
        assertMoney("3", newerPosition.getQuantityAvailable());
        assertMoney("1", newerPosition.getQuantityBlocked());
        assertMoney("10186", userWallet.getBalanceAvailable());
        assertEquals(1, transactionDAO.getTransactionStockBlocks(transactionId).size());
        assertMoney("1", transactionDAO.getTransactionStockBlocks(transactionId).get(0).getQuantity());

        transactionExecutionService.cancelTransaction(transactionId);

        assertMoney("4", newerPosition.getQuantityAvailable());
        assertMoney("0", newerPosition.getQuantityBlocked());
        assertTrue(transactionDAO.getTransactionStockBlocks(transactionId).isEmpty());
        assertMoney("10186", userWallet.getBalanceAvailable());
    }

    @Test
    void shouldSellOnlyFromRequestedPositionWhenOtherSellTransactionBlocksOlderPosition() {
        final UserStockPosition olderPosition = savePosition("5", "20.00", 1);
        final UserStockPosition newerPosition = savePosition("5", "22.00", 2);

        final long olderPositionSellId = createSell("30.00", "5", null).transactionId();
        final long newerPositionSellId = createSell("30.00", "2", newerPosition.getId()).transactionId();

        applyFill(newerPositionSellId, "2", "30.00");

        assertEquals(TransactionStatus.FILLED, getTransaction(newerPositionSellId).getTransactionStatus());
        assertMoney("3", newerPosition.getQuantityAvailable());
        assertMoney("0", newerPosition.getQuantityBlocked());
        assertMoney("5", olderPosition.getQuantityBlocked());
        assertEquals(TransactionStatus.NEW, getTransaction(olderPositionSellId).getTransactionStatus());
    }

    @Test
    void shouldModifyPurchaseByCancelingItAndCreatingNewTransaction() {
        final long transactionId = createPurchase("25.00", "10").transactionId();

        final TransactionResultDTO result = transactionExecutionService.modifyTransaction(transactionId, new BigDecimal("20.00"), new BigDecimal("5"));

        assertEquals(TransactionStatus.CANCELED, getTransaction(transactionId).getTransactionStatus());
        assertNotEquals(transactionId, result.transactionId());
        assertEquals(TransactionStatus.NEW, result.transactionStatus());
        assertEquals(TransactionType.PURCHASE, result.transactionType());
        assertEquals(TICKER, result.stockTicker());
        assertMoney("20", result.stockPriceLimit());
        assertMoney("5", result.stockQuantity());
        assertMoney("9900", userWallet.getBalanceAvailable());
        assertMoney("100", userWallet.getBalanceBlocked());

        assertEquals(3, publishedEvents.size());
        assertInstanceOf(TransactionOpenedEvent.class, publishedEvents.get(0));
        assertInstanceOf(TransactionClosedEvent.class, publishedEvents.get(1));
        assertInstanceOf(TransactionOpenedEvent.class, publishedEvents.get(2));
    }

    @Test
    void shouldModifySellAndKeepRequestedPosition() {
        savePosition("5", "20.00", 1);
        final UserStockPosition newerPosition = savePosition("5", "22.00", 2);
        final long transactionId = createSell("30.00", "2", newerPosition.getId()).transactionId();

        final TransactionResultDTO result = transactionExecutionService.modifyTransaction(transactionId, new BigDecimal("31.00"), new BigDecimal("3"));

        assertEquals(newerPosition.getId(), getTransaction(result.transactionId()).getSellPositionId().orElseThrow());
        assertMoney("2", newerPosition.getQuantityAvailable());
        assertMoney("3", newerPosition.getQuantityBlocked());
    }

    @Test
    void shouldNotModifyWhenNewTransactionCannotBeCreated() {
        final long transactionId = createPurchase("25.00", "10").transactionId();

        // In the application the cancel is rolled back together with the failed create, because both run in one database transaction.
        assertThrows(NotEnoughMoneyException.class,
            () -> transactionExecutionService.modifyTransaction(transactionId, new BigDecimal("25.00"), new BigDecimal("1000")));
    }

    @Test
    void shouldSkipFillOfCanceledTransactionAndReturnItForReload() {
        final long transactionId = createPurchase("25.00", "10").transactionId();
        transactionExecutionService.cancelTransaction(transactionId);

        final List<Long> result = transactionFillService.applyFills(userWallet.getId(), List.of(fill(transactionId, "4", "24.00")));

        assertEquals(List.of(transactionId), result);
        assertTrue(transactionDAO.getAllTransactionExecutions().isEmpty());
        assertMoney("10000", userWallet.getBalanceAvailable());
    }

    @Test
    void shouldCapFillAtRemainingQuantityAndReturnItForReload() {
        final long transactionId = createPurchase("25.00", "10").transactionId();

        final List<Long> result = transactionFillService.applyFills(userWallet.getId(), List.of(fill(transactionId, "12", "25.00")));

        assertEquals(List.of(transactionId), result);
        assertEquals(TransactionStatus.FILLED, getTransaction(transactionId).getTransactionStatus());
        assertMoney("10", getTransaction(transactionId).getFilledQuantity());
    }

    @Test
    void shouldNotFillPurchaseAbovePriceLimit() {
        final long transactionId = createPurchase("25.00", "10").transactionId();
        final Fill fill = fill(transactionId, "1", "25.01");

        assertThrows(IllegalArgumentException.class, () -> transactionFillService.applyFills(userWallet.getId(), List.of(fill)));
    }

    @Test
    void shouldNotProcessSellWithPurchaseProcessor() {
        savePosition("5", "20.00", 1);
        final Transaction sell = getTransaction(createSell("30.00", "1", null).transactionId());

        assertThrows(WrongTransactionTypeForProcessingException.class, () -> purchaseTransactionProcessor.cancelTransaction(sell));
    }

    @Test
    void shouldReturnTransactionsOfWalletNewestFirstWithExecutions() {
        final long firstTransactionId = createPurchase("25.00", "10").transactionId();
        applyFill(firstTransactionId, "4", "24.00");
        final long secondTransactionId = createPurchase("30.00", "1").transactionId();

        final List<TransactionResultDTO> result = transactionExecutionService.getTransactions();

        assertEquals(List.of(secondTransactionId, firstTransactionId), result.stream().map(TransactionResultDTO::transactionId).toList());
        assertEquals(1, result.get(1).executions().size());
        assertMoney("24", result.get(1).executions().get(0).price());
    }

    private TransactionResultDTO createPurchase(final String price, final String quantity) {
        return transactionExecutionService.createTransaction(
            new TransactionRequestDTO(TransactionType.PURCHASE, TICKER, new BigDecimal(price), new BigDecimal(quantity)), "USD");
    }

    private TransactionResultDTO createSell(final String price, final String quantity, final Long positionId) {
        return transactionExecutionService.createTransaction(
            new TransactionRequestDTO(TransactionType.SELL, TICKER, new BigDecimal(price), new BigDecimal(quantity), positionId), "USD");
    }

    private void applyFill(final long transactionId, final String quantity, final String price) {
        assertTrue(transactionFillService.applyFills(userWallet.getId(), List.of(fill(transactionId, quantity, price))).isEmpty());
    }

    private Fill fill(final long transactionId, final String quantity, final String price) {
        return new Fill(transactionId, userWallet.getId(), new BigDecimal(quantity), new BigDecimal(price), NOW.minusSeconds(1));
    }

    private UserStockPosition savePosition(final String quantity, final String openPrice, final int openDayOffset) {
        final UserStockPosition position = UserStockPosition.create(userWallet.getId(), TICKER, "USD", new BigDecimal(quantity),
            new BigDecimal(openPrice), LocalDateTime.now(CLOCK).minusDays(10 - openDayOffset));
        userWalletDAO.saveUserStockPosition(position);

        return position;
    }

    private Transaction getTransaction(final long transactionId) {
        return transactionDAO.getTransactionById(transactionId).orElseThrow();
    }

    private static void assertMoney(final String expected, final BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "Expected: " + expected + " but was: " + actual);
    }
}
