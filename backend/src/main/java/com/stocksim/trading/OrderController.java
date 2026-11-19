package com.stocksim.trading;

import java.math.BigDecimal;
import java.util.List;

import com.stocksim.auth.AppUser;
import com.stocksim.common.ApiException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class OrderController {

	record PlaceOrderRequest(@NotBlank String symbol, @NotNull OrderSide side, @NotNull BigDecimal qty) {
	}

	private final TradingService trading;

	OrderController(TradingService trading) {
		this.trading = trading;
	}

	/**
	 * Places a market order. Business rejections (market closed, insufficient funds, ...) return 200 with
	 * {@code status = REJECTED}. Repeating a request with the same Idempotency-Key returns the first result.
	 */
	@PostMapping
	OrderView place(@AuthenticationPrincipal AppUser user,
			@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody PlaceOrderRequest req) {
		if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 64) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required (max 64 chars)");
		}
		return trading.placeMarketOrder(user.id(), req.symbol(), req.side(), req.qty(), idempotencyKey);
	}

	@GetMapping
	List<OrderView> history(@AuthenticationPrincipal AppUser user, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return trading.history(user.id(), page, size);
	}
}
