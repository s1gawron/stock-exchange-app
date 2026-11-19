package com.stocksim.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** Same-origin only (Spring's default origin check). Auth comes from the session cookie (see SecurityConfig). */
@Configuration(proxyBeanMethods = false)
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

	private final PriceHub hub;

	WebSocketConfig(PriceHub hub) {
		this.hub = hub;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(hub, "/ws");
	}
}
