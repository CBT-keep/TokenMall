package com.tokenmall.common.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogicalCacheSupport {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('DEL', KEYS[1]) else return 0 end",
            Long.class
    );
    private static final int MAX_WAIT_ATTEMPTS = 10;
    private static final long WAIT_INTERVAL_MILLIS = 50;
    private static final long REFRESH_LOCK_TTL_SECONDS = 10;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskExecutor cacheRefreshExecutor;

    /**
     * 读取逻辑过期缓存。缓存已逻辑过期时先返回旧值，再异步刷新；
     * Key 不存在时同步回源，避免冷启动阶段所有请求同时打到数据库。
     */
    public <T> T getOrLoad(
            String key,
            TypeReference<LogicalCacheValue<T>> typeReference,
            long logicalTtlSeconds,
            long physicalTtlSeconds,
            Supplier<T> loader
    ) {
        String json = redisTemplate.opsForValue().get(key);
        LogicalCacheValue<T> cached = parseOrDelete(key, json, typeReference);
        if (cached != null) {
            if (cached.expireAt() > System.currentTimeMillis()) {
                return cached.data();
            }
            refreshAsync(key, typeReference, logicalTtlSeconds, physicalTtlSeconds, loader);
            return cached.data();
        }
        return loadSynchronously(key, typeReference, logicalTtlSeconds, physicalTtlSeconds, loader);
    }

    private <T> T loadSynchronously(
            String key,
            TypeReference<LogicalCacheValue<T>> typeReference,
            long logicalTtlSeconds,
            long physicalTtlSeconds,
            Supplier<T> loader
    ) {
        String lockKey = RedisKeys.cacheRefreshLock(key);
        String lockValue = UUID.randomUUID().toString();
        boolean locked = tryLock(lockKey, lockValue);

        if (!locked) {
            for (int attempt = 0; attempt < MAX_WAIT_ATTEMPTS; attempt++) {
                sleep();
                String json = redisTemplate.opsForValue().get(key);
                LogicalCacheValue<T> cached = parseOrDelete(key, json, typeReference);
                if (cached != null) {
                    return cached.data();
                }
            }
            return loadAndWrite(key, typeReference, logicalTtlSeconds, physicalTtlSeconds, loader, false);
        }

        try {
            String json = redisTemplate.opsForValue().get(key);
            LogicalCacheValue<T> cached = parseOrDelete(key, json, typeReference);
            if (cached != null) {
                return cached.data();
            }
            return loadAndWrite(key, typeReference, logicalTtlSeconds, physicalTtlSeconds, loader, false);
        }
        finally {
            unlock(lockKey, lockValue);
        }
    }

    private <T> void refreshAsync(
            String key,
            TypeReference<LogicalCacheValue<T>> typeReference,
            long logicalTtlSeconds,
            long physicalTtlSeconds,
            Supplier<T> loader
    ) {
        String lockKey = RedisKeys.cacheRefreshLock(key);
        String lockValue = UUID.randomUUID().toString();
        if (!tryLock(lockKey, lockValue)) {
            return;
        }

        try {
            cacheRefreshExecutor.execute(() -> refreshWithLock(
                    key,
                    typeReference,
                    logicalTtlSeconds,
                    physicalTtlSeconds,
                    loader,
                    lockKey,
                    lockValue
            ));
        }
        catch (RuntimeException exception) {
            unlock(lockKey, lockValue);
            log.warn("Logical cache refresh rejected. key={}", key, exception);
        }
    }

    private <T> void refreshWithLock(
            String key,
            TypeReference<LogicalCacheValue<T>> typeReference,
            long logicalTtlSeconds,
            long physicalTtlSeconds,
            Supplier<T> loader,
            String lockKey,
            String lockValue
    ) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            LogicalCacheValue<T> cached = parseOrDelete(key, json, typeReference);
            if (cached != null && cached.expireAt() > System.currentTimeMillis()) {
                return;
            }
            loadAndWrite(key, typeReference, logicalTtlSeconds, physicalTtlSeconds, loader, true);
        }
        catch (RuntimeException exception) {
            log.warn("Logical cache refresh failed, stale value remains. key={}", key, exception);
        }
        finally {
            unlock(lockKey, lockValue);
        }
    }

    private <T> T loadAndWrite(
            String key,
            TypeReference<LogicalCacheValue<T>> typeReference,
            long logicalTtlSeconds,
            long physicalTtlSeconds,
            Supplier<T> loader,
            boolean onlyIfPresent
    ) {
        T value = loader.get();
        if (value == null) {
            redisTemplate.delete(key);
            return null;
        }
        write(key, value, logicalTtlSeconds, physicalTtlSeconds, onlyIfPresent);
        return value;
    }

    private <T> void write(
            String key,
            T value,
            long logicalTtlSeconds,
            long physicalTtlSeconds,
            boolean onlyIfPresent
    ) {
        long logicalTtl = Math.max(1, CacheTtl.jitterSeconds(logicalTtlSeconds));
        long physicalTtlBase = Math.max(physicalTtlSeconds, logicalTtl + 1);
        long physicalTtl = Math.max(logicalTtl + 1, CacheTtl.jitterSeconds(physicalTtlBase));
        long expireAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(logicalTtl);

        try {
            String json = objectMapper.writeValueAsString(new LogicalCacheValue<>(value, expireAt));
            if (onlyIfPresent) {
                redisTemplate.opsForValue().setIfPresent(key, json, physicalTtl, TimeUnit.SECONDS);
            }
            else {
                redisTemplate.opsForValue().set(key, json, physicalTtl, TimeUnit.SECONDS);
            }
        }
        catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "缓存数据序列化失败");
        }
    }

    private <T> LogicalCacheValue<T> parseOrDelete(
            String key,
            String json,
            TypeReference<LogicalCacheValue<T>> typeReference
    ) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            LogicalCacheValue<T> cached = objectMapper.readValue(json, typeReference);
            if (cached == null || cached.data() == null || cached.expireAt() <= 0) {
                deleteQuietly(key);
                return null;
            }
            return cached;
        }
        catch (JsonProcessingException exception) {
            log.warn("Invalid logical cache value, deleting key. key={}", key, exception);
            deleteQuietly(key);
            return null;
        }
    }

    private boolean tryLock(String lockKey, String lockValue) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    lockValue,
                    REFRESH_LOCK_TTL_SECONDS,
                    TimeUnit.SECONDS
            ));
        }
        catch (RuntimeException exception) {
            log.warn("Failed to acquire logical cache refresh lock. key={}", lockKey, exception);
            return false;
        }
    }

    private void unlock(String lockKey, String lockValue) {
        try {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), lockValue);
        }
        catch (RuntimeException exception) {
            log.warn("Failed to release logical cache refresh lock. key={}", lockKey, exception);
        }
    }

    private void deleteQuietly(String key) {
        try {
            redisTemplate.delete(key);
        }
        catch (RuntimeException exception) {
            log.warn("Failed to delete invalid logical cache. key={}", key, exception);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(WAIT_INTERVAL_MILLIS);
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "等待缓存重建被中断");
        }
    }
}
