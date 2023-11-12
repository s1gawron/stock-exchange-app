package com.s1gawron.stockexchange.shared;

public record ErrorResponse(String timestamp, int code, String error, String message, String URI) {

}
