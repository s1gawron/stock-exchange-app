package com.s1gawron.stockexchange.stock.controller;

import com.s1gawron.stockexchange.shared.ErrorResponse;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.exception.FinnhubConnectionFailedException;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

public class StockErrorHandlerController {

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
}
