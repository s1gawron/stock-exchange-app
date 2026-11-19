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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

class AuthApiTest extends IntegrationTest {

	@Autowired
	MockMvc mvc;

	private static String registerJson(String email) {
		return """
				{"email":"%s","displayName":"Trader","password":"correct-horse"}
				""".formatted(email);
	}

	@Test
	void registerLogsInAndUserCanTradeAndLogOut() throws Exception {
		String email = UUID.randomUUID() + "@test.local";
		MockHttpSession session = new MockHttpSession();

		mvc.perform(post("/api/auth/register").session(session).with(csrf())
			.contentType(MediaType.APPLICATION_JSON).content(registerJson(email)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value(email));

		mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
		mvc.perform(get("/api/portfolio").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.cash").value(100000.00));

		mvc.perform(post("/api/orders").session(session).with(csrf())
			.header("Idempotency-Key", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON).content("""
					{"symbol":"msft","side":"BUY","qty":2}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("FILLED"))
			.andExpect(jsonPath("$.symbol").value("MSFT"));

		mvc.perform(get("/api/orders").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));

		mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
		mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
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
		MockHttpSession session = new MockHttpSession();
		mvc.perform(post("/api/auth/register").session(session).with(csrf())
			.contentType(MediaType.APPLICATION_JSON).content(registerJson(UUID.randomUUID() + "@test.local")))
			.andExpect(status().isCreated());
		mvc.perform(post("/api/orders").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
			.content("{\"symbol\":\"AAPL\",\"side\":\"BUY\",\"qty\":1}"))
			.andExpect(status().isBadRequest());
	}
}
