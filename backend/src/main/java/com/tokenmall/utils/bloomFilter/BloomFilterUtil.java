package com.tokenmall.utils.bloomFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;

/**
 * 布隆过滤器初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterUtil {


    private static final long BIT_SIZE = 1024 * 1024;
    private static final int[] SEEDS = {5, 7, 11};
    private static final String READY_SUFFIX = ":ready";
    private final StringRedisTemplate redisTemplate;

    // 添加元素
    public void add(String key, String value) {
        long[] indexes = getIndexes(value);
        for (long idx : indexes) {
            redisTemplate.opsForValue().setBit(key, idx, true);
        }
    }

    // 批量添加元素
    public void addAll(String key, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (String value : values) {
            add(key, value);
        }
    }

    /**
     * 重建过滤器。先在临时 key 上构建，构建完成后原子替换，
     * 避免重建期间出现“过滤器存在但只写入了一部分元素”的误判。
     * 替换完成后再写 ready 标记；即使没有元素也写一个 0 位，
     * 保证位图 key 存在，便于判断过滤器是否可用。
     */
    public void rebuild(String key, Collection<String> values) {
        String tempKey = key + ":rebuild:" + UUID.randomUUID();
        try {
            redisTemplate.opsForValue().setBit(tempKey, 0, false);
            addAll(tempKey, values);
            redisTemplate.rename(tempKey, key);
            redisTemplate.opsForValue().set(key + READY_SUFFIX, "1");
        }
        catch (RuntimeException exception) {
            redisTemplate.delete(tempKey);
            throw exception;
        }
    }

    // 判断是否存在
    public boolean contains(String key, String value) {
        long[] indexes = getIndexes(value);
        for (long idx : indexes) {
            // 只要有一位是 false，就一定不存在
            if (!Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, idx))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 仅在过滤器可用且明确判断元素不存在时返回 true。
     * Redis 异常或位图 key 丢失时返回 false，交给数据库兜底，
     * 避免把实际存在的 ID 全部误判为不存在。
     */
    public boolean definitelyAbsent(String key, String value) {
        try {
            if (!isReady(key)) {
                return false;
            }
            long[] indexes = getIndexes(value);
            for (long idx : indexes) {
                if (!Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, idx))) {
                    return true;
                }
            }
            return false;
        }
        catch (RuntimeException exception) {
            log.warn("Bloom filter unavailable, fall back to database. key={}", key, exception);
            return false;
        }
    }

    /**
     * 只有完成全量预热后写入的位图才可信；位图丢失或被清空时返回 false。
     */
    public boolean isReady(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key))
                    && Boolean.TRUE.equals(redisTemplate.hasKey(key + READY_SUFFIX));
        }
        catch (RuntimeException exception) {
            log.warn("Bloom filter readiness check failed. key={}", key, exception);
            return false;
        }
    }

    // 计算哈希索引
    private long[] getIndexes(String value) {
        long[] indexes = new long[SEEDS.length];
        for (int i = 0; i < SEEDS.length; i++) {
            long hash = hash(value, SEEDS[i]);
            indexes[i] = hash % BIT_SIZE; // 取模保证索引在位图范围内
        }
        return indexes;
    }

    // 哈希算法
    private long hash(String value, int seed) {
        long result = 0;
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            result = result * seed + b;
        }
        return result & Long.MAX_VALUE; // 保证是正数
    }
}
