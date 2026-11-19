package com.stocksim.competition;

import java.time.LocalDate;
import java.util.List;

import com.stocksim.auth.AppUser;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/competitions")
class CompetitionController {

	record CreateRequest(@NotBlank @Size(max = 100) String name, @NotNull LocalDate startsOn,
			@NotNull LocalDate endsOn) {
	}

	record JoinRequest(@NotBlank @Size(max = 16) String code) {
	}

	private final CompetitionService service;

	CompetitionController(CompetitionService service) {
		this.service = service;
	}

	@GetMapping
	List<CompetitionService.Summary> mine(@AuthenticationPrincipal AppUser user) {
		return service.mine(user.id());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	CompetitionService.Summary create(@AuthenticationPrincipal AppUser user, @Valid @RequestBody CreateRequest req) {
		return service.create(user.id(), req.name(), req.startsOn(), req.endsOn());
	}

	@PostMapping("/join")
	CompetitionService.Summary join(@AuthenticationPrincipal AppUser user, @Valid @RequestBody JoinRequest req) {
		return service.join(user.id(), req.code());
	}

	@GetMapping("/{id}")
	CompetitionService.Details details(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
		return service.details(user.id(), id);
	}

	@PostMapping("/{id}/leave")
	ResponseEntity<Void> leave(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
		service.leave(user.id(), id);
		return ResponseEntity.noContent().build();
	}
}
