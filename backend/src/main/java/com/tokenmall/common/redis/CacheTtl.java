package com.tokenmall.common.redis;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 为缓存 TTL 增加随机抖动，避免同一批 key 在同一时刻集中失效。
 */
public final class CacheTtl {

    private static final double JITTER_RATIO = 0.2;

    public static long jitterSeconds(long baseSeconds) {
        if (baseSeconds <= 0) {
            return baseSeconds;
        }
        long jitter = (long) (baseSeconds * JITTER_RATIO);
        if (jitter == 0) {
            return baseSeconds;
        }
        return baseSeconds - jitter + ThreadLocalRandom.current().nextLong(jitter * 2 + 1);
    }

    private CacheTtl() {
    }
}
