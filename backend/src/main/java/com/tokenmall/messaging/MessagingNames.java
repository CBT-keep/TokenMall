package com.tokenmall.messaging;

public final class MessagingNames {

    public static final String ORDER_EXCHANGE = "mall.order.exchange";
    public static final String TOKEN_EXCHANGE = "mall.token.exchange";
    public static final String SECKILL_EXCHANGE = "mall.seckill.exchange";
    public static final String DELAY_EXCHANGE = "mall.delay.exchange";
    public static final String DLX_EXCHANGE = "mall.dlx.exchange";

    public static final String TOKEN_GRANT_QUEUE = "mall.token.grant.queue";
    public static final String ORDER_CREATED_QUEUE = "mall.order.created.queue";
    public static final String CACHE_INVALIDATION_QUEUE = "mall.cache.invalidation.queue";
    public static final String ORDER_TIMEOUT_DELAY_QUEUE = "mall.order.timeout.delay.queue";
    public static final String ORDER_TIMEOUT_QUEUE = "mall.order.timeout.queue";
    public static final String SECKILL_ORDER_QUEUE = "mall.seckill.order.queue";
    public static final String TOKEN_GRANT_DLQ = "mall.token.grant.dlq";
    public static final String SECKILL_ORDER_DLQ = "mall.seckill.order.dlq";

    public static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String PRODUCT_CHANGED_ROUTING_KEY = "product.changed";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";
    public static final String TOKEN_GRANT_FAILED_ROUTING_KEY = "token.grant.failed";
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order";
    public static final String SECKILL_ORDER_FAILED_ROUTING_KEY = "seckill.order.failed";

    private MessagingNames() {
    }
}
