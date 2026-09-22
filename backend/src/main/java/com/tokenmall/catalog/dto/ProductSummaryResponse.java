package com.tokenmall.catalog.dto;

import com.tokenmall.catalog.entity.Product;

import java.math.BigDecimal;

/**
 * 商品的摘要信息
 */
public record ProductSummaryResponse(
        Long id,
        Long categoryId,
        String productType,
        String name,
        String subtitle,
        String coverUrl,
        BigDecimal price,
        BigDecimal originalPrice,
        Long tokenAmount,
        Integer planDays,
        Long planQuota,
        Integer status,
        Integer sortOrder
) {

    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getCategoryId(),
                product.getProductType(),
                product.getName(),
                product.getSubtitle(),
                product.getCoverUrl(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getTokenAmount(),
                product.getPlanDays(),
                product.getPlanQuota(),
                product.getStatus(),
                product.getSortOrder()
        );
    }
}
