package com.s1gawron.stockexchange.shared;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

public abstract class AbstractErrorHandlerController {

    @ExceptionHandler(UserUnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse userUnauthorizedExceptionExceptionHandler(final UserUnauthenticatedException userUnauthenticatedException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            userUnauthenticatedException.getMessage(), httpServletRequest.getRequestURI());
    }

}
