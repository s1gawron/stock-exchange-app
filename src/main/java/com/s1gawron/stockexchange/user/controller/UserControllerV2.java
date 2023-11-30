package com.s1gawron.stockexchange.user.controller;

import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user/v2")
public class UserControllerV2 extends UserErrorHandlerController {

    private final UserWalletService userWalletService;

    public UserControllerV2(final UserWalletService userWalletService) {
        this.userWalletService = userWalletService;
    }

    @GetMapping("wallet")
    public UserWalletDTO getUserWalletDetails() {
        return userWalletService.updateAndGetUserWallet();
    }

}
