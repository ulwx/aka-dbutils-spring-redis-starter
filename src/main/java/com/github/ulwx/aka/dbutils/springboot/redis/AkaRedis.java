package com.github.ulwx.aka.dbutils.springboot.redis;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AkaRedis {

    String value() default "default";
}
