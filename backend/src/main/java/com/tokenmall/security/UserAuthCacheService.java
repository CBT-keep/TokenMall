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

    public UserAuthSnapshot get(Long userId) {
        UserAuthSnapshot local = localCache.getIfPresent(userId);
        if (local != null) {
            log.debug("User auth cache hit: local userId={}", userId);
            return local;
        }

        UserAuthSnapshot redis = getFromRedis(userId);
        if (redis != null) {
            localCache.put(userId, redis);
            log.debug("User auth cache hit: redis userId={}", userId);
            return redis;
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserAuthSnapshot loaded = UserAuthSnapshot.from(user);
        put(loaded);
        log.debug("User auth cache miss: loaded from mysql userId={}", userId);
        return loaded;
    }

    public void put(UserAuthSnapshot snapshot) {
        localCache.put(snapshot.userId(), snapshot);
        putToRedis(snapshot);
    }

    public void evict(Long userId) {
        localCache.invalidate(userId);
        try {
            redisTemplate.delete(redisKey(userId));
            log.debug("User auth cache evicted userId={}", userId);
        } catch (DataAccessException exception) {
            log.warn("Failed to evict user auth cache from Redis: userId={}", userId, exception);
        }
    }

    private UserAuthSnapshot getFromRedis(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(redisKey(userId));
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, UserAuthSnapshot.class);
        } catch (JsonProcessingException exception) {
            log.warn("Invalid user auth cache JSON, deleting key: userId={}", userId, exception);
            redisTemplate.delete(redisKey(userId));
            return null;
        } catch (DataAccessException exception) {
            log.warn("Redis unavailable while reading user auth cache: userId={}", userId, exception);
            return null;
        }
    }

    private void putToRedis(UserAuthSnapshot snapshot) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            redisTemplate.opsForValue().set(
                    redisKey(snapshot.userId()),
                    json,
                    Duration.ofSeconds(properties.getRedisTtlSeconds())
            );
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize user auth cache: userId={}", snapshot.userId(), exception);
        } catch (DataAccessException exception) {
            log.warn("Redis unavailable while writing user auth cache: userId={}", snapshot.userId(), exception);
        }
    }

    private String redisKey(Long userId) {
        return REDIS_KEY_PREFIX + userId;
    }
}
