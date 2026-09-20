package com.tokenmall.seckill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SeckillActivityRequest(
        @NotNull Long productId,
        @NotNull Long skuId,
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal seckillPrice,
        @NotNull @Min(1) Integer seckillStock,
        @NotNull @Min(1) Integer perUserLimit,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        String status
) {
}
