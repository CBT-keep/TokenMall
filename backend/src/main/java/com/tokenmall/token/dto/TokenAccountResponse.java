package com.tokenmall.token.dto;

import com.tokenmall.user.entity.UserTokenAccount;

public record TokenAccountResponse(
        Long packBalance,
        Long planBalance,
        Long totalPurchased,
        Long totalConsumed
) {

    public static TokenAccountResponse from(UserTokenAccount account) {
        return new TokenAccountResponse(
                account.getPackBalance(),
                account.getPlanBalance(),
                account.getTotalPurchased(),
                account.getTotalConsumed()
        );
    }
}
