package com.stocksim.marketdata;

public record CompanyProfile(String name, String exchange, String currency, String logoUrl, String webUrl,
		String industry) {
}
