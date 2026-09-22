package com.tokenmall.seckill.dto;

import com.tokenmall.seckill.entity.SeckillActivity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SeckillActivityResponse(
        Long id,
        Long productId,
        Long skuId,
        String name,
        BigDecimal seckillPrice,
        Integer seckillStock,
        Integer soldCount,
        Integer perUserLimit,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {
    // 构建器方法
    public static SeckillActivityResponse from(SeckillActivity activity) {
        return new SeckillActivityResponse(
                activity.getId(),
                activity.getProductId(),
                activity.getSkuId(),
                activity.getName(),
                activity.getSeckillPrice(),
                activity.getSeckillStock(),
                activity.getSoldCount(),
                activity.getPerUserLimit(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getStatus()
        );
    }
}
