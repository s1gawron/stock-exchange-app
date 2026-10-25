package com.s1gawron.stockexchange.shared;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.exception.FinnhubConnectionFailedException;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;

public abstract class AbstractErrorHandlerController {

    private static final Logger log = LoggerFactory.getLogger(AbstractErrorHandlerController.class);

    @ExceptionHandler(UserUnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse userUnauthorizedExceptionExceptionHandler(final UserUnauthenticatedException userUnauthenticatedException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            userUnauthenticatedException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(StockNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse stockNotFoundExceptionHandler(final StockNotFoundException stockNotFoundException, final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
            stockNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(FinnhubConnectionFailedException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse finnhubConnectionFailedExceptionHandler(final FinnhubConnectionFailedException finnhubConnectionFailedException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(),
            finnhubConnectionFailedException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse resourceAccessExceptionHandler(final ResourceAccessException resourceAccessException, final HttpServletRequest httpServletRequest) {
        log.warn("External service could not be reached", resourceAccessException);
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase(),
            "External service could not be reached! Please try again.", httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserWalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userWalletNotFoundExceptionHandler(final UserWalletNotFoundException userWalletNotFoundException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
            userWalletNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse dataIntegrityViolationExceptionHandler(final DataIntegrityViolationException dataIntegrityViolationException,
        final HttpServletRequest httpServletRequest) {
        log.warn("Data integrity violation", dataIntegrityViolationException);
        return new ErrorResponse(Instant.now().toString(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
            "Request conflicts with the current state of the resource! Please try again.", httpServletRequest.getRequestURI());
    }

}
