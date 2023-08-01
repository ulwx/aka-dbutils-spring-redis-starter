package com.github.ulwx.aka.dbutils.springboot.redis;

import com.ulwx.tool.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Map;

/**
 * 自定义数据源连接工厂，实现多数据源切换，利用AkaRedisSelectHolder获取Redis数据源标识，实现多数据源切换
 */
public class MultiRedisLettuceConnectionFactory
        implements InitializingBean, DisposableBean,
        RedisConnectionFactory,
        ReactiveRedisConnectionFactory {

    public Map<String, LettuceConnectionFactory> getConnectionFactoryMap() {
        return connectionFactoryMap;
    }

    private final Map<String, LettuceConnectionFactory> connectionFactoryMap;


    public MultiRedisLettuceConnectionFactory(Map<String, LettuceConnectionFactory> connectionFactoryMap) {
        this.connectionFactoryMap = connectionFactoryMap;
    }

    @Override
    public void destroy() {
        connectionFactoryMap.values().forEach(LettuceConnectionFactory::destroy);
    }

    @Override
    public void afterPropertiesSet() {
        connectionFactoryMap.values().forEach(LettuceConnectionFactory::afterPropertiesSet);
    }

    private LettuceConnectionFactory currentLettuceConnectionFactory() {
        String ds = AkaRedisSelector.get();
        if (StringUtils.hasText(ds)) {
            LettuceConnectionFactory factory = connectionFactoryMap.get(ds);
            return factory;
        }
        return connectionFactoryMap.get(AkaMultiRedisProperties.DEFAULT);
    }

    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        return currentLettuceConnectionFactory().getReactiveConnection();
    }

    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        return currentLettuceConnectionFactory().getReactiveClusterConnection();
    }

    @Override
    public RedisConnection getConnection() {
        return currentLettuceConnectionFactory().getConnection();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return currentLettuceConnectionFactory().getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return currentLettuceConnectionFactory().getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return currentLettuceConnectionFactory().getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return currentLettuceConnectionFactory().translateExceptionIfPossible(ex);
    }
}


