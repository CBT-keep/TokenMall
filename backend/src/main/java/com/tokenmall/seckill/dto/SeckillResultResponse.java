package com.tokenmall.seckill.dto;

public record SeckillResultResponse(
        boolean success,
        String requestId,
        String orderNo,
        String status,
        String message
) {
}
