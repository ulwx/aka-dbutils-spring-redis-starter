package com.github.ulwx.aka.dbutils.springboot.redis;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;


@ConfigurationProperties(prefix = "aka.springboot")
public class AkaMultiRedisProperties {
    public static final String DEFAULT = "default";
    private boolean enableMulti = false;
    private Map<String, RedisProperties> redis=new HashMap<>();

    public Map<String, RedisProperties> getRedis() {

        return redis;
    }

    public void setRedis(Map<String, RedisProperties> redis) {
        this.redis = redis;
    }
}
