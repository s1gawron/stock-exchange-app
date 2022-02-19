package pl.eizodev.app.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.eizodev.app.shared.ErrorResponse;
import pl.eizodev.app.user.exception.AccountNotFoundException;
import pl.eizodev.app.user.exception.NullUserRegisterPropertiesException;
import pl.eizodev.app.user.exception.UserEmailExistsException;
import pl.eizodev.app.user.exception.UserNameExistsException;

import javax.servlet.http.HttpServletRequest;

public abstract class UserErrorHandlerController {

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

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse accountNotFoundExceptionHandler(final AccountNotFoundException accountNotFoundException,
                                                         final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                accountNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(NullUserRegisterPropertiesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse nullUserRegisterPropertiesExceptionHandler(final NullUserRegisterPropertiesException nullUserRegisterPropertiesException,
                                                                    final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                nullUserRegisterPropertiesException.getMessage(), httpServletRequest.getRequestURI());
    }
}
