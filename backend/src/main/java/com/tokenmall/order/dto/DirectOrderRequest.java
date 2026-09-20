package com.tokenmall.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DirectOrderRequest(
        @NotNull Long skuId,
        @NotNull @Min(1) Integer quantity,
        @NotBlank String requestId
) {
}
