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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class OrderController {

	/** {@code type} defaults to MARKET; {@code timeInForce} defaults to DAY for LIMIT/STOP. */
	record PlaceOrderRequest(@NotBlank String symbol, @NotNull OrderSide side, OrderType type,
			@NotNull BigDecimal qty, BigDecimal limitPrice, BigDecimal stopPrice, TimeInForce timeInForce) {
	}

	private final TradingService trading;

	OrderController(TradingService trading) {
		this.trading = trading;
	}

	/**
	 * Places an order. Business rejections (market closed, insufficient funds, ...) return 200 with
	 * {@code status = REJECTED}. Limit/stop orders return {@code OPEN} while they wait. Repeating a request with the
	 * same Idempotency-Key returns the first result.
	 */
	@PostMapping
	OrderView place(@AuthenticationPrincipal AppUser user,
			@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody PlaceOrderRequest req) {
		if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 64) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required (max 64 chars)");
		}
		var cmd = new TradingService.PlaceOrder(req.symbol(), req.side(),
				req.type() == null ? OrderType.MARKET : req.type(), req.qty(), req.limitPrice(), req.stopPrice(),
				req.timeInForce());
		return trading.placeOrder(user.id(), cmd, idempotencyKey);
	}

	@PostMapping("/{id}/cancel")
	OrderView cancel(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
		return trading.cancel(user.id(), id);
	}

	@GetMapping
	List<OrderView> history(@AuthenticationPrincipal AppUser user, @RequestParam(required = false) OrderStatus status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return trading.history(user.id(), status, page, size);
	}
}
