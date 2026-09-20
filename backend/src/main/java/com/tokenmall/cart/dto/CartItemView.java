package com.tokenmall.cart.dto;

import java.math.BigDecimal;

public record CartItemView(
        Long id,
        Long skuId,
        Long productId,
        String productName,
        String skuName,
        String productType,
        BigDecimal price,
        Integer quantity,
        Integer selected,
        Integer availableStock,
        Long tokenAmount,
        Integer planDays,
        Long planQuota
) {
}
