package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

/**
 * Opens a websocket connection. Separated from the feed so tests can use a fake connection.
 */
@FunctionalInterface
public interface WebSocketConnector {

    CompletableFuture<WebSocket> connect(URI uri, WebSocket.Listener listener);

}
