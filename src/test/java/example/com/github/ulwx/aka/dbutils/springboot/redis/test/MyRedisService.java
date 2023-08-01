package example.com.github.ulwx.aka.dbutils.springboot.redis.test;

import com.github.ulwx.aka.dbutils.springboot.redis.AkaRedis;
import com.github.ulwx.aka.dbutils.springboot.redis.AkaRedisUtils;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
public class MyRedisService {
    @Autowired
    private AkaRedisUtils akaRedisUtils;

    @AkaRedis("one")
    public void setForOne(String key,String value){
        akaRedisUtils.set(key, value);
    }
    public void set(String key,String value){
        akaRedisUtils.set(key, value);
    }
    @AkaRedis("one")
    public String  getForOne(String key){
        return akaRedisUtils.get(key);
    }

    public String  get(String key){
        return akaRedisUtils.get(key);
    }
}
