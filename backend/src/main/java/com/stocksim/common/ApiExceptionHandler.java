package com.stocksim.common;

import com.stocksim.marketdata.MarketDataUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ProblemDetail api(ApiException ex) {
		return ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage());
	}

	@ExceptionHandler(MarketDataUnavailableException.class)
	ProblemDetail marketData(MarketDataUnavailableException ex) {
		log.warn("Market data unavailable: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"Market data is unavailable right now. Try again shortly.");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	ProblemDetail duplicate(DuplicateKeyException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The resource already exists");
	}
}
