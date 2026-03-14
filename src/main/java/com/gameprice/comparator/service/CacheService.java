package com.gameprice.comparator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service("redisCacheService")
@RequiredArgsConstructor
@Slf4j
@Profile("!local")
public class CacheService implements ICacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long TTL_HOURS = 6;

    public <T> void cache(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, value, TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached value for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to cache value for key: {}", key, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
                return Optional.of((T) value);
            }
            log.debug("Cache miss for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to get value from cache for key: {}", key, e);
        }
        return Optional.empty();
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache for key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to evict cache for key: {}", key, e);
        }
    }

    public void evictPattern(String pattern) {
        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Evicted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache for pattern: {}", pattern, e);
        }
    }
}