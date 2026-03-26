package com.s1gawron.stockexchange.user.stock.favourite.controller;

import com.s1gawron.stockexchange.shared.AbstractErrorHandlerController;
import com.s1gawron.stockexchange.shared.ErrorResponse;
import com.s1gawron.stockexchange.user.stock.favourite.exception.UserFavouriteStockAlreadyExistsException;
import com.s1gawron.stockexchange.user.stock.favourite.exception.UserFavouriteStockNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

public abstract class UserFavouriteStockErrorHandlerController extends AbstractErrorHandlerController {

    @ExceptionHandler(UserFavouriteStockAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userFavouriteStockAlreadyExistsExceptionHandler(final UserFavouriteStockAlreadyExistsException exception,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
            exception.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserFavouriteStockNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userFavouriteStockNotFoundExceptionHandler(final UserFavouriteStockNotFoundException exception,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
            exception.getMessage(), httpServletRequest.getRequestURI());
    }
}
