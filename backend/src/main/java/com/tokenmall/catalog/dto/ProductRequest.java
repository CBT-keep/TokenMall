package com.tokenmall.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @NotNull Long categoryId,
        @NotBlank String productType,
        @NotBlank String name,
        String subtitle,
        String description,
        String coverUrl,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        BigDecimal originalPrice,
        Long tokenAmount,
        Integer planDays,
        Long planQuota,
        @PositiveOrZero Integer purchaseLimit,
        Integer status,
        Integer sortOrder
) {
}
