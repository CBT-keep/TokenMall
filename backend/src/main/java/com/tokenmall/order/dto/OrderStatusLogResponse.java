package com.tokenmall.order.dto;

import com.tokenmall.order.entity.OrderStatusLog;

import java.time.LocalDateTime;

public record OrderStatusLogResponse(
        String fromStatus,
        String toStatus,
        String operatorType,
        String remark,
        LocalDateTime createdAt
) {

    public static OrderStatusLogResponse from(OrderStatusLog log) {
        return new OrderStatusLogResponse(
                log.getFromStatus(),
                log.getToStatus(),
                log.getOperatorType(),
                log.getRemark(),
                log.getCreatedAt()
        );
    }
}
