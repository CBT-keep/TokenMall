package com.tokenmall.order.dto;

import com.tokenmall.order.entity.MallOrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        Long skuId,
        String productType,
        String productName,
        String skuName,
        BigDecimal unitPrice,
        Integer quantity,
        Long tokenAmount,
        Integer planDays,
        Long planQuota
) {

    public static OrderItemResponse from(MallOrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSkuId(),
                item.getProductType(),
                item.getProductName(),
                item.getSkuName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTokenAmount(),
                item.getPlanDays(),
                item.getPlanQuota()
        );
    }
}
