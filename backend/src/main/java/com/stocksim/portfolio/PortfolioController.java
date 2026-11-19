package com.stocksim.portfolio;

import com.stocksim.auth.AppUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
class PortfolioController {

	private final PortfolioService service;

	PortfolioController(PortfolioService service) {
		this.service = service;
	}

	@GetMapping
	PortfolioService.Portfolio get(@AuthenticationPrincipal AppUser user) {
		return service.get(user.id());
	}
}
