package com.tokenmall.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SkuRequest(
        @NotNull Long productId,
        @NotBlank String skuCode,
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        Long tokenAmount,
        Integer planDays,
        Long planQuota,
        Integer status
) {
}
