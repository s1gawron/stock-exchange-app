package pl.eizodev.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.eizodev.app.service.exception.*;
import pl.eizodev.app.service.exception.error.ErrorResponse;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractErrorHandlerController {

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse accountNotFoundExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                         final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(StockNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse stockNotFoundExceptionHandler(final StockNotFoundException stockNotFoundException,
                                                       final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                stockNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NoSuchStockException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse noSuchStockExceptionHandler(final NoSuchStockException noSuchStockException,
                                                     final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                noSuchStockException.getMessage(), httpServletRequest.getRequestURI());
    }

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

    @ExceptionHandler(UserEmailExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userEmailExistsExceptionHandler(final UserEmailExistsException userEmailExistsException,
                                                         final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                userEmailExistsException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserNameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userNameExistsExceptionHandler(final UserNameExistsException userNameExistsException,
                                                        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                userNameExistsException.getMessage(), httpServletRequest.getRequestURI());
    }
}
