package com.stocksim.trading;

/** Published after an order changed state outside the user's own request (fill by matcher, expiry). */
public record OrderUpdatedEvent(long userId, OrderView order) {
}
