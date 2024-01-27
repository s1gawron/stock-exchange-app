package com.s1gawron.stockexchange.transaction.controller;

import com.s1gawron.stockexchange.shared.AbstractErrorHandlerController;
import com.s1gawron.stockexchange.shared.ErrorResponse;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.transaction.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

public abstract class TransactionErrorHandlerController extends AbstractErrorHandlerController {

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

    @ExceptionHandler(StockPurchasePriceLessThanZeroException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse stockPurchasePriceLessThanZeroExceptionHandler(final StockPurchasePriceLessThanZeroException stockPurchasePriceLessThanZeroException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            stockPurchasePriceLessThanZeroException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(WrongStockQuantityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse wrongStockQuantityExceptionHandler(final WrongStockQuantityException wrongStockQuantityException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            wrongStockQuantityException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(TransactionInfoNotCollectedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse transactionInfoNotCollectedExceptionHandler(final WrongStockQuantityException transactionInfoNotCollectedException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            transactionInfoNotCollectedException.getMessage(), httpServletRequest.getRequestURI());
    }
}
