package pl.eizodev.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.eizodev.app.services.exceptions.*;
import pl.eizodev.app.services.exceptions.errors.ErrorResponse;

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
    public ErrorResponse stockNotFoundExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                       final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NoSuchStockException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse noSuchStockExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                     final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NotEnoughMoneyException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ErrorResponse notEnoughMoneyExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.PAYMENT_REQUIRED.value(), HttpStatus.PAYMENT_REQUIRED.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NotEnoughStockException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse notEnoughStockExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserEmailExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userEmailExistsExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                         final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserNameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userNameExistsExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }
}
