package com.gameprice.comparator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service("cacheService")
@Profile("local")
@Slf4j
public class LocalCacheService implements ICacheService {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long TTL_HOURS = 6;

    public <T> void cache(String key, T value) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(TTL_HOURS)));
        log.debug("Cached value for key: {}", key);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit for key: {}", key);
            return Optional.of((T) entry.value);
        }
        log.debug("Cache miss for key: {}", key);
        return Optional.empty();
    }

    public void evict(String key) {
        cache.remove(key);
        log.debug("Evicted cache for key: {}", key);
    }

    public void evictPattern(String pattern) {
        String regex = pattern.replace("*", ".*");
        cache.keySet().removeIf(k -> k.matches(regex));
        log.debug("Evicted keys matching pattern: {}", pattern);
    }

    private static class CacheEntry {
        final Object value;
        final long expiresAt;

        CacheEntry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
