package com.tokenmall.catalog.dto;

import com.tokenmall.catalog.entity.ProductSku;

import java.math.BigDecimal;

public record SkuResponse(
        Long id,
        Long productId,
        String skuCode,
        String name,
        BigDecimal price,
        Long tokenAmount,
        Integer planDays,
        Long planQuota,
        Integer status,
        Integer totalStock,
        Integer availableStock,
        Integer lockedStock
) {

    public static SkuResponse from(ProductSku sku, Integer totalStock, Integer availableStock, Integer lockedStock) {
        return new SkuResponse(
                sku.getId(),
                sku.getProductId(),
                sku.getSkuCode(),
                sku.getName(),
                sku.getPrice(),
                sku.getTokenAmount(),
                sku.getPlanDays(),
                sku.getPlanQuota(),
                sku.getStatus(),
                totalStock,
                availableStock,
                lockedStock
        );
    }
}
