package com.haotiao.proxy.cache;

import java.util.Set;

public interface CacheService {
    void set(String key, Object value);

    void set(String key, Object value, int expireTime);

    void setExpireTime(String key, int expireTime);

    <T>T get(String key, Class<T> requiredType);

    Object get(String key);

    <T>T get(String key, CacheLoader<T> cacheLoader);

    int getExpireTime(String key);

    void del(String key);

    Set<String> keys(String pattern);

    void lpush(String key, Object value);

    <T>T lpop(String key, Class<T> requireType);

    Object lpop(String key);

    long llen(String key);
}
