package com.github.ulwx.aka.dbutils.springboot.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnExpression("#{T(com.github.ulwx.aka.dbutils.springboot.redis.AkaRedisAutoConfiguration).valid(environment)}")
@EnableConfigurationProperties(AkaMultiRedisProperties.class)
@PropertySource(value = {"classpath*:aka-application-redis.yml"},
        name = "classpath*:aka-application-redis.yml",
        factory = MyPropertySourceFactory.class)
@Import(AkaRedisSelectAspect.class)
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class AkaRedisAutoConfiguration {
    @Autowired
    private ApplicationContext applicationContext;

    public static boolean valid(Environment environment){
        Map<String, String> mapProperty = Binder.get(environment).bind("aka.springboot.redis", Map.class).orElse(null);
        if(mapProperty==null) return false;
        if(!mapProperty.isEmpty()){
            return true;
        }
        return false;

    }
    @Bean
    @ConditionalOnMissingBean
    public AkaRedisUtils akaRedisUtils(@Qualifier("akaStringRedisTemplate") StringRedisTemplate akaStringRedisTemplate){
        return new AkaRedisUtils(akaStringRedisTemplate);
    }
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> akaRedisTemplate(MultiRedisLettuceConnectionFactory
                                                                      redisConnectionFactory) {
        RedisSerializer<Object> serializer = redisSerializer();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate akaStringRedisTemplate(MultiRedisLettuceConnectionFactory
                                                                  redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
    public RedisSerializer<Object> redisSerializer() {
        // 创建JSON序列化器
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 必须设置，否则无法将json转化为对象，会转化为Map类型
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiRedisLettuceConnectionFactory akaMultiRedisLettuceConnectionFactory(
            AkaMultiRedisProperties multiRedisProperties) {
        //读取配置
        Map<String, LettuceConnectionFactory> connectionFactoryMap = new HashMap<>();
        Map<String, RedisProperties> multi = multiRedisProperties.getRedis();
        multi.forEach((k, v) -> {
            LettuceConnectionConfiguration lettuceConnectionConfiguration=
                    new LettuceConnectionConfiguration(v);
            LettuceConnectionFactory lettuceConnectionFactory =
                    lettuceConnectionConfiguration.createRedisConnectionFactory();
            connectionFactoryMap.put(k, lettuceConnectionFactory);
        });
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }

}
