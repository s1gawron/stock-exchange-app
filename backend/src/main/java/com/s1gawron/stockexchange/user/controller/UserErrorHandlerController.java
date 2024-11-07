package com.s1gawron.stockexchange.user.controller;

import com.s1gawron.stockexchange.shared.AbstractErrorHandlerController;
import com.s1gawron.stockexchange.shared.ErrorResponse;
import com.s1gawron.stockexchange.user.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

public abstract class UserErrorHandlerController extends AbstractErrorHandlerController {

    @ExceptionHandler(UserEmailExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userEmailExistsExceptionHandler(final UserEmailExistsException userEmailExistsException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
            userEmailExistsException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserNameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse userNameExistsExceptionHandler(final UserNameExistsException userNameExistsException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
            userNameExistsException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserRegisterEmptyPropertiesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse userEmptyRegisterPropertiesExceptionHandler(final UserRegisterEmptyPropertiesException userRegisterEmptyPropertiesException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            userRegisterEmptyPropertiesException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserEmailPatternViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse userEmailPatternViolationExceptionHandler(final UserEmailPatternViolationException userEmailPatternViolationException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            userEmailPatternViolationException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserPasswordTooWeakException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse userPasswordTooWeakExceptionHandler(final UserPasswordTooWeakException userPasswordTooWeakException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            userPasswordTooWeakException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserWalletBalanceLessThanZeroException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse userWalletBalanceLessThanZeroExceptionHandler(final UserWalletBalanceLessThanZeroException userWalletBalanceLessThanZeroException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
            userWalletBalanceLessThanZeroException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(UserWalletNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userWalletNotFoundExceptionHandler(final UserWalletNotFoundException userWalletNotFoundException,
        final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
            userWalletNotFoundException.getMessage(), httpServletRequest.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse authenticationExceptionHandler(final AuthenticationException authenticationException, final HttpServletRequest httpServletRequest) {
        return new ErrorResponse(Instant.now().toString(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            authenticationException.getMessage(), httpServletRequest.getRequestURI());
    }

}
