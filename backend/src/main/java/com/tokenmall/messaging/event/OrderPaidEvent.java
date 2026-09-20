package com.tokenmall.messaging.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderPaidEvent(
        String orderNo,
        Long userId,
        BigDecimal payAmount,
        List<OrderPaidItem> items
) {

    public record OrderPaidItem(
            Long orderItemId,
            Long productId,
            Long skuId,
            String productType,
            Integer quantity,
            Long tokenAmount,
            Integer planDays,
            Long planQuota
    ) {
    }
}
