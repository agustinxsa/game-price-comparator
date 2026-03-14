package com.gameprice.comparator.service;

import java.util.Optional;

public interface ICacheService {
    <T> void cache(String key, T value);
    <T> Optional<T> get(String key, Class<T> type);
    void evict(String key);
    void evictPattern(String pattern);
}
