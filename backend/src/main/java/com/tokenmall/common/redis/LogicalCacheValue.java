package com.tokenmall.common.redis;

public record LogicalCacheValue<T>(T data, long expireAt) {
}
