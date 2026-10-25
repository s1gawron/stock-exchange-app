package com.s1gawron.stockexchange.user.model;

import java.util.List;

public record UserWalletSnapshot(UserWallet userWallet, List<UserStock> userStocks) {

}
