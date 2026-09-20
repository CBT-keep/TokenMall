package com.tokenmall.token.dto;

import com.tokenmall.token.entity.TokenTransaction;

import java.time.LocalDateTime;

public record TokenTransactionResponse(
        Long id,
        String transactionNo,
        Long orderId,
        Long planId,
        String balanceType,
        String transactionType,
        Long amount,
        Long balanceAfter,
        String description,
        LocalDateTime createdAt
) {

    public static TokenTransactionResponse from(TokenTransaction transaction) {
        return new TokenTransactionResponse(
                transaction.getId(),
                transaction.getTransactionNo(),
                transaction.getOrderId(),
                transaction.getPlanId(),
                transaction.getBalanceType(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
