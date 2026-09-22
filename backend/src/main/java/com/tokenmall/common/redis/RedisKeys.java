package com.tokenmall.common.redis;

public final class RedisKeys {

    public static final String PRODUCT_DETAIL = "mall:product:detail:";
    public static final String SKU_DETAIL = "mall:sku:detail:";
    public static final String PRODUCT_LIST = "mall:product:list:";
    public static final String SECKILL_ACTIVITY = "mall:seckill:activity:";
    public static final String SECKILL_STOCK = "mall:seckill:stock:";
    public static final String SECKILL_USER = "mall:seckill:user:";
    public static final String LOCK_ORDER = "mall:lock:order:";
    public static final String LOCK_SECKILL = "mall:lock:seckill:";
    public static final String IDEMPOTENCY_ORDER = "mall:idempotency:order:";
    public static final String RATE_SECKILL = "mall:rate:seckill:";
    public static final String TOKEN_ACCOUNT = "mall:token:account:";

    private RedisKeys() {
    }

    public static String productDetail(Long productId) {
        return PRODUCT_DETAIL + productId;
    }

    public static String seckillStock(Long activityId) {
        return SECKILL_STOCK + activityId;
    }

    public static String seckillUser(Long activityId, Long userId) {
        return SECKILL_USER + activityId + ":" + userId;
    }
}
