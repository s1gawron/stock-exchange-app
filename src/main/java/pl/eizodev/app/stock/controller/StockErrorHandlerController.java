package pl.eizodev.app.stock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.eizodev.app.shared.ErrorResponse;
import pl.eizodev.app.stock.exception.StockNotFoundException;

import javax.servlet.http.HttpServletRequest;

public class StockErrorHandlerController {

    @ExceptionHandler(StockNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse stockNotFoundExceptionHandler(final StockNotFoundException stockNotFoundException,
                                                       final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                stockNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }
}
