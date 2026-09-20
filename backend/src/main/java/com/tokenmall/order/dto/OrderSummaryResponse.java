package com.tokenmall.order.dto;

import com.tokenmall.order.entity.MallOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderNo,
        String orderType,
        Long activityId,
        BigDecimal payAmount,
        String status,
        LocalDateTime expireTime,
        LocalDateTime createdAt
) {

    public static OrderSummaryResponse from(MallOrder order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNo(),
                order.getOrderType(),
                order.getActivityId(),
                order.getPayAmount(),
                order.getStatus(),
                order.getExpireTime(),
                order.getCreatedAt()
        );
    }
}
