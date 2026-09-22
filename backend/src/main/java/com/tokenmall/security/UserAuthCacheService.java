package com.tokenmall.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.tokenmall.user.entity.SysUser;
import com.tokenmall.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthCacheService {

    private static final String REDIS_KEY_PREFIX = "mall:user:auth:";

    private final Cache<Long, UserAuthSnapshot> localCache;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SysUserMapper sysUserMapper;
    private final UserAuthCacheProperties properties;

    // 获取用户认证快照
    public UserAuthSnapshot get(Long userId) {
        UserAuthSnapshot local = localCache.getIfPresent(userId);
        if (local != null) {
            log.debug("缓存命中: 本地用户ID为={}", userId);
            return local;
        }

        // 本地缓存未命中，尝试从Redis缓存中获取
        UserAuthSnapshot redis = getFromRedis(userId);
        if (redis != null) {
            localCache.put(userId, redis);
            log.debug("缓存命中: Redis用户ID为={}", userId);
            return redis;
        }

        // Redis缓存未命中，从数据库中加载
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // 创建用户认证快照
        UserAuthSnapshot loaded = UserAuthSnapshot.from(user);
        put(loaded);
        log.debug("缓存未命中: 从MySQL加载用户ID为={}", userId);
        return loaded;
    }

    // 将用户认证快照放入缓存
    public void put(UserAuthSnapshot snapshot) {
        localCache.put(snapshot.userId(), snapshot);
        putToRedis(snapshot);
    }

    // 从缓存中移除用户认证快照
    public void evict(Long userId) {
        localCache.invalidate(userId);
        try {
            redisTemplate.delete(redisKey(userId));
            log.debug("缓存移除: 用户ID为={}", userId);
        } catch (DataAccessException exception) {
            log.warn("缓存移除失败: Redis用户ID为={}", userId, exception);
        }
    }

    // 从Redis中获取用户认证快照
    private UserAuthSnapshot getFromRedis(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(redisKey(userId));
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, UserAuthSnapshot.class);
        } catch (JsonProcessingException exception) {
            log.warn("缓存JSON无效，删除密钥: 用户ID为={}", userId, exception);
            redisTemplate.delete(redisKey(userId));
            return null;
        } catch (DataAccessException exception) {
            log.warn("Redis不可用，读取用户认证缓存: 用户ID为={}", userId, exception);
            return null;
        }
    }

    // 将用户认证快照放入Redis
    private void putToRedis(UserAuthSnapshot snapshot) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            redisTemplate.opsForValue().set(
                    redisKey(snapshot.userId()),
                    json,
                    Duration.ofSeconds(properties.getRedisTtlSeconds())
            );
        } catch (JsonProcessingException exception) {
            log.warn("缓存序列化失败: 用户ID为={}", snapshot.userId(), exception);
        } catch (DataAccessException exception) {
            log.warn("Redis不可用，写入用户认证缓存: 用户ID为={}", snapshot.userId(), exception);
        }
    }

    // 生成Redis缓存键
    private String redisKey(Long userId) {
        return REDIS_KEY_PREFIX + userId;
    }
}
