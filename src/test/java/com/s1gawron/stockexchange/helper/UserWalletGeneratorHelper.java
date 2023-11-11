package com.s1gawron.stockexchange.helper;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public enum UserWalletGeneratorHelper {

    I;

    public UserWallet getUserWalletWithNoStock(final String username, final BigDecimal balanceAvailable, final BigDecimal previousWalletValue) {
        final String userEmail = username + "@test.pl";
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, userEmail, "password", balanceAvailable);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, balanceAvailable);

        userWallet.setPreviousWalletValue(previousWalletValue);
        userWallet.setLastUpdateDate(LocalDateTime.of(2022, 4, 11, 17, 56, 22));

        return userWallet;
    }

    public UserWallet getUserWalletWithStock(final String username, final BigDecimal balanceAvailable, final BigDecimal previousWalletValue) {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(username, "test@test.pl", "password", balanceAvailable);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, balanceAvailable);

        userWallet.setPreviousWalletValue(previousWalletValue);
        userWallet.setLastUpdateDate(LocalDateTime.of(2022, 4, 11, 17, 56, 22));
        userWallet.addUserStocks(getUserStocks());

        return userWallet;
    }

    private List<UserStock> getUserStocks() {
        return List.of(
            new UserStock("AAPL", new BigDecimal("25.00"), 100),
            new UserStock("AMZN", new BigDecimal("32.00"), 150)
        );
    }

    public List<UserWallet> getUserWalletsWithNoStock(final List<BigDecimal> availableBalances, final List<BigDecimal> previousWalletValues) {
        validateLists(availableBalances, previousWalletValues);

        final UserWallet userWallet = getUserWalletWithNoStock("user", availableBalances.get(0), previousWalletValues.get(0));
        final UserWallet userWallet1 = getUserWalletWithNoStock("user1", availableBalances.get(1), previousWalletValues.get(1));
        final UserWallet userWallet2 = getUserWalletWithNoStock("user2", availableBalances.get(2), previousWalletValues.get(2));

        return List.of(userWallet, userWallet1, userWallet2);
    }

    public List<UserWallet> getUserWalletsWithStock(final List<BigDecimal> availableBalances, final List<BigDecimal> previousWalletValues) {
        validateLists(availableBalances, previousWalletValues);

        final UserWallet userWallet = getUserWalletWithStock("user", availableBalances.get(0), previousWalletValues.get(0));
        final UserWallet userWallet1 = getUserWalletWithStock("user1", availableBalances.get(1), previousWalletValues.get(1));
        final UserWallet userWallet2 = getUserWalletWithStock("user2", availableBalances.get(2), previousWalletValues.get(2));

        return List.of(userWallet, userWallet1, userWallet2);
    }

    private void validateLists(final List<BigDecimal> userWalletBalances, final List<BigDecimal> previousWalletValues) {
        if (userWalletBalances.size() != 3 || previousWalletValues.size() != 3) {
            throw new UnsupportedOperationException("Lists must have 3 elements!");
        }
    }

}
