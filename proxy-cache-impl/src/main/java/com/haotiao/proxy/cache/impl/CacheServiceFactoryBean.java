package com.haotiao.proxy.cache.impl;

import com.haotiao.proxy.cache.CacheService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Properties;

public class CacheServiceFactoryBean implements FactoryBean, ApplicationContextAware {
    private String strategy;
    private CacheService cacheService;
    private Properties properties;

    public Object getObject() throws Exception {
        if (cacheService != null) {
            return cacheService;
        }
        try {
            this.cacheService = (CacheServiceEx) Class.forName("com.haotiao.proxy.cache.impl.RedisCacheService").newInstance();
        } catch (Throwable e) {

        }
        return cacheService;
    }


    public Class<?> getObjectType() {
        return CacheServiceEx.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Properties mergedProps = new Properties();
        Map<String, Properties> propsMapping = applicationContext.getBeansOfType(Properties.class);
        if (properties != null) {
            CollectionUtils.mergePropertiesIntoMap(this.properties, mergedProps);
        }
        if (propsMapping != null) {
            for (Properties props : propsMapping.values()) {
                CollectionUtils.mergePropertiesIntoMap(props, mergedProps);
            }
        }
        CacheServiceEx cacheServiceEx = null;
        try {
            cacheServiceEx = (CacheServiceEx) getObject();
        } catch (Exception e) {
            throw new BeanCreationException("create CacheServiceEx error", e);
        }
        cacheServiceEx.setConfig(mergedProps);
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void startup() {
        CacheServiceEx cacheServiceEx = null;
        try {
            cacheServiceEx = (CacheServiceEx) getObject();
        } catch (Exception e) {
            throw new BeanCreationException("create CacheServiceEx error", e);
        }
        cacheServiceEx.startup();
    }

    public void shutdown() {
        CacheServiceEx cacheServiceEx = null;
        try {
            cacheServiceEx = (CacheServiceEx) getObject();
        } catch (Exception e) {
            throw new BeanCreationException("create CacheServiceEx error", e);
        }
        cacheServiceEx.shutdown();
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
