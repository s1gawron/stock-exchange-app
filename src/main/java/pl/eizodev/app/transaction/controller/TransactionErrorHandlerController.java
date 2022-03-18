package pl.eizodev.app.transaction.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.eizodev.app.shared.ErrorResponse;
import pl.eizodev.app.stock.exception.StockNotFoundException;
import pl.eizodev.app.transaction.exception.NoStockInUserWalletException;
import pl.eizodev.app.transaction.exception.NotEnoughMoneyException;
import pl.eizodev.app.transaction.exception.NotEnoughStockException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

public abstract class TransactionErrorHandlerController {

    @ExceptionHandler(NotEnoughMoneyException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ErrorResponse notEnoughMoneyExceptionHandler(final NotEnoughMoneyException notEnoughMoneyException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.PAYMENT_REQUIRED.value(), HttpStatus.PAYMENT_REQUIRED.getReasonPhrase(),
            notEnoughMoneyException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NotEnoughStockException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse notEnoughStockExceptionHandler(final NotEnoughStockException notEnoughStockException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
            notEnoughStockException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NoStockInUserWalletException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse noSuchStockExceptionHandler(final NoStockInUserWalletException noStockInUserWalletException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
            noStockInUserWalletException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(StockNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse stockNotFoundExceptionHandler(final StockNotFoundException stockNotFoundException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            stockNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }
}
