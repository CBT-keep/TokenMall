package com.tokenmall.order.dto;

import com.tokenmall.order.entity.MallOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderNo,
        String orderType,
        Long activityId,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        String status,
        LocalDateTime payTime,
        LocalDateTime expireTime,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        List<OrderStatusLogResponse> statusLogs
) {

    public static OrderDetailResponse from(
            MallOrder order,
            List<OrderItemResponse> items,
            List<OrderStatusLogResponse> statusLogs
    ) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNo(),
                order.getOrderType(),
                order.getActivityId(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getStatus(),
                order.getPayTime(),
                order.getExpireTime(),
                order.getCreatedAt(),
                items,
                statusLogs
        );
    }
}
