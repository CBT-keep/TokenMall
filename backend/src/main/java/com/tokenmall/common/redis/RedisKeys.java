package com.tokenmall.common.redis;

public final class RedisKeys {

    public static final String PRODUCT_DETAIL = "mall:product:detail:";
    public static final String SKU_DETAIL = "mall:sku:detail:";
    public static final String PRODUCT_LIST = "mall:product:list:";
    public static final String SECKILL_ACTIVITY = "mall:seckill:activity:";
    public static final String SECKILL_LIST = "mall:seckill:list:";
    public static final String SECKILL_LIST_ALL = "mall:seckill:list:all:";
    public static final String SECKILL_RECORDS = "mall:seckill:records:";
    public static final int SECKILL_TTL = 30;
    public static final String SECKILL_RESULT = "mall:seckill:result:";
    public static final String SECKILL_STOCK = "mall:seckill:stock:";
    public static final String SECKILL_USER = "mall:seckill:user:";
    // 与 SECKILL_SCRIPT 中的 Hash 字段名保持一致
    public static final String SECKILL_USER_QUANTITY_FIELD = "quantity";
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

    public static String seckillActivity(Long activityId) {
        return SECKILL_ACTIVITY + activityId;
    }

    public static String seckillRecords(Long activityId) {
        return SECKILL_RECORDS + activityId;
    }

    public static String seckillResult(String requestId, Long userId) {
        return SECKILL_RESULT + requestId + ":" + userId;
    }
}
