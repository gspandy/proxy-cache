package com.haotiao.proxy.cache.impl;

import com.haotiao.proxy.cache.CacheLoader;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Transaction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;

public class RedisCacheService implements CacheServiceEx {
    private JedisPool jedisPool;
    private Properties config;

    public void set(String key, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key.getBytes(), SerializationUtils.serialize(value));
        } catch (Exception e) {
            throw new RedisException("set key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, Object value, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Transaction tx = jedis.multi();
            byte[] keycode = key.getBytes();
            tx.set(keycode,SerializationUtils.serialize(value));
            tx.expire(keycode, expireTime);
            tx.exec();
            tx.close();
        } catch (Exception e) {
            throw new RedisException("set key[" + key + "] expireTime[" + expireTime + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setExpireTime(String key, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.expire(key.getBytes(), expireTime);
        } catch (Exception e) {
            throw new RedisException("set expire time[" + expireTime + "] for key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public <T> T get(String key, Class<T> requiredType) {
        return (T) getObj(key);
    }

    private Object getObj(String key) {
        Object target = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] value = jedis.get(key.getBytes());
            if (value != null) {
                target = SerializationUtils.deserialize(value);
            }
        } catch (Exception e) {
            throw new RedisException("get key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return target;
    }

    public <T> T get(String key, CacheLoader<T> cacheLoader) {
        T target = (T) this.getObj(key);
        if (target == null) {
            target = cacheLoader.load(key);
            if (target != null) {
                this.set(key, target);
            }
        }
        return target;
    }

    public int getExpireTime(String key) {
        int target = -1;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            target = jedis.ttl(key.getBytes()).intValue();
        } catch (Exception e) {
            throw new RedisException("get expire time for key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return target;
    }

    public void del(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key.getBytes());
        } catch (Exception e) {
            throw new RedisException("del key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public Set<String> keys(String pattern) {
        Set<String> keys = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            keys = jedis.keys(pattern);
        } catch (Exception e) {
            throw new RedisException("list keys[" + pattern + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return keys;
    }

    public void lpush(String key, Object value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.lpush(key.getBytes(), SerializationUtils.serialize(value));
        } catch (Exception e) {
            throw new RedisException("push key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public <T> T lpop(String key, Class<T> requireType) {
        return (T) lpop(key);
    }

    public long llen(String key) {
        long length = 0;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            length = jedis.llen(key.getBytes());
        } catch (Exception e) {
            throw new RedisException("llen for key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return length;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public Object get(String key) {
        return this.getObj(key);
    }

    public Object lpop(String key) {
        Object target = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            byte[] data = jedis.lpop(key.getBytes());
            if (data != null) {
                target = SerializationUtils.deserialize(jedis.lpop(key.getBytes()));
            }
        } catch (Exception e) {
            throw new RedisException("pop key[" + key + "] error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return target;
    }

    public void startup() {
        if (this.jedisPool != null) {
            return;
        }
        GenericObjectPoolConfig redisConfig = new GenericObjectPoolConfig();
        URI redisUri = null;
        String redisCfgUrl = config.getProperty("cache.redis.url");
        try {
            redisUri = new URI(redisCfgUrl);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("[" + redisCfgUrl + "] parsed error", e);
        }
        jedisPool = new JedisPool(redisConfig, redisUri.getHost(), redisUri.getPort(), 3000, redisUri.getUserInfo(), Protocol.DEFAULT_DATABASE);
    }

    public void shutdown() {
        jedisPool.close();
    }

    public boolean support(String strategy) {
        return "Redis".equals(strategy);
    }
}
