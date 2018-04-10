package com.haotiao.proxy.cache.impl;

import com.haotiao.proxy.cache.CacheService;

import java.util.Properties;

public interface CacheServiceEx extends CacheService {
    void setConfig(Properties config);

    void startup();

    void shutdown();

    boolean support(String strategy);
}
