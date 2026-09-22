package com.tokenmall.catalog.dto;

import com.tokenmall.catalog.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
        Long id,
        Long categoryId,
        String productType,
        String name,
        String subtitle,
        String description,
        String coverUrl,
        BigDecimal price,
        BigDecimal originalPrice,
        Long tokenAmount,
        Integer planDays,
        Long planQuota,
        Integer purchaseLimit,
        Integer status,
        List<SkuResponse> skus  // SKU是商品的销售属性
) {

    // 根据商品和SKU列表，返回商品详情响应对象
    public static ProductDetailResponse from(Product product, List<SkuResponse> skus) {
        return new ProductDetailResponse(
                product.getId(),
                product.getCategoryId(),
                product.getProductType(),
                product.getName(),
                product.getSubtitle(),
                product.getDescription(),
                product.getCoverUrl(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getTokenAmount(),
                product.getPlanDays(),
                product.getPlanQuota(),
                product.getPurchaseLimit(),
                product.getStatus(),
                skus
        );
    }
}
