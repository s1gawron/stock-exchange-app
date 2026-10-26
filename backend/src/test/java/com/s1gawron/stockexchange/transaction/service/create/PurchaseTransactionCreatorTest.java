package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.transaction.dao.impl.InMemoryTransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionCreatorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private UserWalletService userWalletServiceMock;

    private InMemoryTransactionDAO transactionDAO;

    private PurchaseTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAO = new InMemoryTransactionDAO();
        underTest = new PurchaseTransactionCreator(userWalletServiceMock, transactionDAO, CLOCK);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeCreated() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        final boolean result = underTest.canCreateTransaction(transactionRequestDTO);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughMoneyToBuyStock() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("100.00"), new BigDecimal("100.00"));

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        final NotEnoughMoneyException exception = assertThrows(NotEnoughMoneyException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
        assertTrue(exception.getMessage().contains("You can buy: 4 of AAPL stock"));
    }

    @Test
    void shouldCreateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        ReflectionTestUtils.setField(userWallet, "id", 1L);

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        underTest.createTransaction(transactionRequestDTO);

        assertEquals(new BigDecimal("12.5000"), userWallet.getBalanceAvailable());
        assertEquals(new BigDecimal("202.5000"), userWallet.getBalanceBlocked());
        assertTrue(transactionDAO.getTransactionById(0L).isPresent());
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).updateUserWallet(userWallet);
    }

}