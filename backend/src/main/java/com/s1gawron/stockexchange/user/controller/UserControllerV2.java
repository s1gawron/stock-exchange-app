package com.s1gawron.stockexchange.user.controller;

import com.s1gawron.stockexchange.user.dto.UserStockDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.service.UserWalletDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/user/v2/wallet")
public class UserControllerV2 extends UserErrorHandlerController {

    private final UserWalletDetailsService userWalletDetailsService;

    public UserControllerV2(final UserWalletDetailsService userWalletDetailsService) {
        this.userWalletDetailsService = userWalletDetailsService;
    }

    @GetMapping
    public UserWalletDTO getUserWalletDetails() {
        return userWalletDetailsService.getUserWalletDTO();
    }

    @GetMapping("stocks")
    public List<UserStockDTO> getUserStocks() {
        return userWalletDetailsService.getUserStocks();
    }

}
