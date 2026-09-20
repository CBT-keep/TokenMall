package com.tokenmall.seckill.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeckillRequest(
        @NotBlank String requestId,
        @NotNull @Min(1) Integer quantity
) {
}
