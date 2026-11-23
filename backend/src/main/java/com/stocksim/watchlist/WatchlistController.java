package com.stocksim.watchlist;

import java.util.List;

import com.stocksim.auth.AppUser;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlist")
class WatchlistController {

	private final WatchlistService service;

	WatchlistController(WatchlistService service) {
		this.service = service;
	}

	@GetMapping
	List<WatchlistService.Item> list(@AuthenticationPrincipal AppUser user) {
		return service.list(user.id());
	}

	@PutMapping("/{symbol}")
	ResponseEntity<Void> add(@AuthenticationPrincipal AppUser user, @PathVariable String symbol) {
		service.add(user.id(), symbol);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{symbol}")
	ResponseEntity<Void> remove(@AuthenticationPrincipal AppUser user, @PathVariable String symbol) {
		service.remove(user.id(), symbol);
		return ResponseEntity.noContent().build();
	}
}
