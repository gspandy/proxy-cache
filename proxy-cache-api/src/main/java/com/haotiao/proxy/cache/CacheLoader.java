package com.haotiao.proxy.cache;

public interface CacheLoader<T> {
    T load(String key);
}
