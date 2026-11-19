package com.stocksim.performance;

import com.stocksim.auth.AppUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class PerformanceController {

	private final AnalyticsService analytics;
	private final LeaderboardService leaderboard;

	PerformanceController(AnalyticsService analytics, LeaderboardService leaderboard) {
		this.analytics = analytics;
		this.leaderboard = leaderboard;
	}

	@GetMapping("/analytics")
	AnalyticsService.Analytics analytics(@AuthenticationPrincipal AppUser user) {
		return analytics.forUser(user.id());
	}

	@GetMapping("/leaderboard")
	LeaderboardService.Leaderboard leaderboard(@AuthenticationPrincipal AppUser user,
			@RequestParam(defaultValue = "50") int limit) {
		return leaderboard.get(user.id(), Math.clamp(limit, 1, 100));
	}
}
