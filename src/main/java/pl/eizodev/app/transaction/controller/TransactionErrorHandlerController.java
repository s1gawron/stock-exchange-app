package pl.eizodev.app.transaction.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.eizodev.app.shared.ErrorResponse;
import pl.eizodev.app.transaction.exception.NoStockInUserWalletException;
import pl.eizodev.app.transaction.exception.NotEnoughMoneyException;
import pl.eizodev.app.transaction.exception.NotEnoughStockException;

import javax.servlet.http.HttpServletRequest;

public abstract class TransactionErrorHandlerController {

    @ExceptionHandler(NotEnoughMoneyException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ErrorResponse notEnoughMoneyExceptionHandler(final NotEnoughMoneyException notEnoughMoneyException,
                                                        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.PAYMENT_REQUIRED.value(), HttpStatus.PAYMENT_REQUIRED.getReasonPhrase(),
                notEnoughMoneyException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NotEnoughStockException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse notEnoughStockExceptionHandler(final NotEnoughStockException notEnoughStockException,
                                                        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                notEnoughStockException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NoStockInUserWalletException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse noSuchStockExceptionHandler(final NoStockInUserWalletException noStockInUserWalletException,
                                                     final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                noStockInUserWalletException.getMessage(), httpServletRequest.getRequestURI());
    }
}
