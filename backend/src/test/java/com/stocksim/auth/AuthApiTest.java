package com.stocksim.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.stocksim.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;

class AuthApiTest extends IntegrationTest {

	@Autowired
	MockMvc mvc;

	/**
	 * Registers (which logs in) and returns the Spring Session cookie, as a browser would keep it. MockMvc shows a
	 * simplified cookie; the real HttpOnly/SameSite header is checked in PriceHubWebSocketTest.
	 */
	private Cookie registerAndGetSession(String email) throws Exception {
		Cookie session = mvc.perform(post("/api/auth/register").with(csrf())
			.contentType(MediaType.APPLICATION_JSON).content(registerJson(email)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value(email))
			.andReturn().getResponse().getCookie("SESSION");
		org.assertj.core.api.Assertions.assertThat(session).as("SESSION cookie").isNotNull();
		return session;
	}

	/** Redis key of a session; the cookie value is the Base64-encoded session id. */
	private static String redisKey(Cookie session) {
		String id = new String(java.util.Base64.getDecoder().decode(session.getValue()));
		return "stocksim:session:sessions:" + id;
	}

	private static String registerJson(String email) {
		return """
				{"email":"%s","displayName":"Trader","password":"correct-horse"}
				""".formatted(email);
	}

	@Test
	void registerLogsInAndUserCanTradeAndLogOut() throws Exception {
		String email = UUID.randomUUID() + "@test.local";
		Cookie session = registerAndGetSession(email);
		org.assertj.core.api.Assertions.assertThat(redis.hasKey(redisKey(session))).as("session in Redis").isTrue();

		mvc.perform(get("/api/auth/me").cookie(session)).andExpect(status().isOk());
		mvc.perform(get("/api/portfolio").cookie(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.cash").value(100000.00));

		mvc.perform(post("/api/orders").cookie(session).with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON).content("""
					{"symbol":"msft","side":"BUY","qty":2}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("FILLED"))
			.andExpect(jsonPath("$.symbol").value("MSFT"));

		mvc.perform(get("/api/orders").cookie(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));

		mvc.perform(post("/api/auth/logout").cookie(session).with(csrf())).andExpect(status().isNoContent());
		org.assertj.core.api.Assertions.assertThat(redis.hasKey(redisKey(session))).as("session removed").isFalse();
		mvc.perform(get("/api/auth/me").cookie(session)).andExpect(status().isUnauthorized());
	}

	@Test
	void storedSessionHoldsNoPasswordHash() throws Exception {
		Cookie session = registerAndGetSession(UUID.randomUUID() + "@test.local");
		var stored = redis.opsForHash().entries(redisKey(session)).toString();
		org.assertj.core.api.Assertions.assertThat(stored).doesNotContain("{bcrypt}");
	}

	@Test
	void apiRequiresLogin() throws Exception {
		mvc.perform(get("/api/portfolio")).andExpect(status().isUnauthorized());
	}

	@Test
	void postWithoutCsrfTokenIsForbidden() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
			.content(registerJson(UUID.randomUUID() + "@test.local")))
			.andExpect(status().isForbidden());
	}

	@Test
	void wrongPasswordAndUnknownEmailGetTheSameAnswer() throws Exception {
		String email = UUID.randomUUID() + "@test.local";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content(registerJson(email))).andExpect(status().isCreated());

		for (String body : new String[] { "{\"email\":\"%s\",\"password\":\"wrong-pass\"}".formatted(email),
				"{\"email\":\"nobody@test.local\",\"password\":\"wrong-pass\"}" }) {
			mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Invalid email or password"));
		}
	}

	@Test
	void duplicateEmailIsRejectedCaseInsensitive() throws Exception {
		String email = UUID.randomUUID() + "@test.local";
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content(registerJson(email))).andExpect(status().isCreated());
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content(registerJson(email.toUpperCase()))).andExpect(status().isConflict());
	}

	@Test
	void validatesRegistrationInput() throws Exception {
		mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"not-an-email\",\"displayName\":\"x\",\"password\":\"short\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void orderWithoutIdempotencyKeyIsBadRequest() throws Exception {
		Cookie session = registerAndGetSession(UUID.randomUUID() + "@test.local");
		mvc.perform(post("/api/orders").cookie(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content("{\"symbol\":\"AAPL\",\"side\":\"BUY\",\"qty\":1}"))
			.andExpect(status().isBadRequest());
	}
}
