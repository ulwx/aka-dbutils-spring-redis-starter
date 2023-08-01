package com.github.ulwx.aka.dbutils.springboot.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class AkaRedisSelectAspect {

    public AkaRedisSelectAspect(){
        int i=0;
    }
    private final String defaultDsName = AkaMultiRedisProperties.DEFAULT;

    /**
     * 创建AkaRedis对应的切面，来对标有注解的方法拦截
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.github.ulwx.aka.dbutils.springboot.redis.AkaRedis)")
    public Object configRedis(ProceedingJoinPoint point) throws Throwable {
        String dsName = defaultDsName;
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();

            AkaRedis config = method.getAnnotation(AkaRedis.class);
            if (config != null) {
                dsName = config.value();
            }
            AkaRedisSelector.push(dsName);
            return point.proceed();
        } finally {
            AkaRedisSelector.pop(dsName);
        }
    }

}
