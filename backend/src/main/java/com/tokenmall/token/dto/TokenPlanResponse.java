package com.tokenmall.token.dto;

import com.tokenmall.token.entity.UserTokenPlan;

import java.time.LocalDateTime;

public record TokenPlanResponse(
        Long id,
        Long orderId,
        Long productId,
        Long skuId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long totalQuota,
        Long usedQuota,
        Long remainingQuota,
        String status
) {

    public static TokenPlanResponse from(UserTokenPlan plan) {
        return new TokenPlanResponse(
                plan.getId(),
                plan.getOrderId(),
                plan.getProductId(),
                plan.getSkuId(),
                plan.getStartTime(),
                plan.getEndTime(),
                plan.getTotalQuota(),
                plan.getUsedQuota(),
                plan.getRemainingQuota(),
                plan.getStatus()
        );
    }
}
