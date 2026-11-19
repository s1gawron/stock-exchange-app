package com.stocksim.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ProblemDetail;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	record RegisterRequest(@NotBlank @Email @Size(max = 320) String email,
			@NotBlank @Size(max = 100) String displayName,
			@NotBlank @Size(min = 8, max = 72) String password) {
	}

	record LoginRequest(@NotBlank String email, @NotBlank String password) {
	}

	record Me(long id, String email, String displayName) {
		static Me of(AppUser u) {
			return new Me(u.id(), u.email(), u.displayName());
		}
	}

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository contextRepository;

	AuthController(UserService userService, AuthenticationManager authenticationManager,
			SecurityContextRepository contextRepository) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.contextRepository = contextRepository;
	}

	/** Lets the SPA obtain the XSRF-TOKEN cookie before its first POST. */
	@GetMapping("/csrf")
	ResponseEntity<Void> csrf(CsrfToken token) {
		token.getToken(); // loading the deferred token makes Spring Security write the cookie
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	Me register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request, HttpServletResponse response) {
		userService.register(req.email(), req.displayName(), req.password());
		return login(new LoginRequest(req.email(), req.password()), request, response);
	}

	@PostMapping("/login")
	Me login(@Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = authenticationManager
			.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(req.email().trim(), req.password()));
		if (request.getSession(false) != null) {
			request.changeSessionId(); // session fixation protection
		}
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);
		contextRepository.saveContext(context, request, response);
		return Me.of((AppUser) auth.getPrincipal());
	}

	@GetMapping("/me")
	Me me(@AuthenticationPrincipal AppUser user) {
		return Me.of(user);
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ProblemDetail> badCredentials(AuthenticationException ex) {
		// Same answer for unknown email and wrong password.
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
	}

	@ExceptionHandler(UserService.EmailTakenException.class)
	ResponseEntity<ProblemDetail> emailTaken(UserService.EmailTakenException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
	}

}
