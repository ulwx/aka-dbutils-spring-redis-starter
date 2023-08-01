package com.github.ulwx.aka.dbutils.springboot.redis;

import com.ulwx.tool.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class AkaRedisUtils  {

    private StringRedisTemplate redisTemplate;
    private final static ThreadLocal<String> LOCAL_DS = new ThreadLocal<String>();

    public static void setDS(String dsName){
        LOCAL_DS.set(dsName);
    }

    public AkaRedisUtils(StringRedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;

    }

    private <T> T DS(Supplier<T> function) {
        String dsName=LOCAL_DS.get();
        if (StringUtils.hasText(dsName)) {
            AkaRedisSelector.push(dsName);
        }
        try {
            return function.get();
        } finally {
            String newStr=LOCAL_DS.get();
            if (StringUtils.hasText(newStr) ) {
                if(newStr.equals(dsName)) {
                    AkaRedisSelector.pop(dsName);
                }else{
                    throw new RuntimeException("内存状态不一致！[dsName="+dsName+",newStr="+newStr+"]");
                }
            }
        }
    }

    private void DS(Call function) {
        String dsName=LOCAL_DS.get();
        if (StringUtils.hasText(dsName)) {
            AkaRedisSelector.push(dsName);
        }
        try {
             function.run();
        } finally {
            String newStr=LOCAL_DS.get();
            if (StringUtils.hasText(newStr) ) {
                if(newStr.equals(dsName)) {
                    AkaRedisSelector.pop(dsName);
                }else{
                    throw new RuntimeException("内存状态不一致！[dsName="+dsName+",newStr="+newStr+"]");
                }
            }
        }
    }

    public static interface Call {
        void run();
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        return DS(() -> {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
                return true;
            } else {
                throw new RuntimeException("超时时间小于0");
            }
        });

    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return DS(() -> {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        });
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        return DS(() -> {
            return redisTemplate.hasKey(key);
        });
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        DS(() -> {
            if (key != null && key.length > 0) {
                if (key.length == 1) {
                    redisTemplate.delete(key[0]);
                } else {
                    List<String> objects = (List<String>) CollectionUtils.arrayToList(key);
                    redisTemplate.delete(objects);
                }
            }
        });
    }


    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return DS(() -> {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        });
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, String value) {
        return DS(() -> {
            redisTemplate.opsForValue().set(key, value);
            return true;
        });
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, String value, long time) {
        return DS(() -> {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                this.set(key, value);
            }
            return true;
        });

    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        return DS(() -> {
            if (delta < 0) {
                throw new RuntimeException("递增因子必须大于0");
            }
            return redisTemplate.opsForValue().increment(key, delta);
        });
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        return DS(() -> {
            if (delta < 0) {
                throw new RuntimeException("递减因子必须大于0");
            }
            return redisTemplate.opsForValue().increment(key, -delta);
        });
    }
    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return DS(() -> {
            return redisTemplate.opsForHash().get(key, item);
        });
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return DS(() -> {
            return redisTemplate.opsForHash().entries(key);
        });
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        return DS(() -> {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        });
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        return DS(() -> {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        });
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        return DS(() -> {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        });
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        return DS(() -> {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        });
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        DS(() -> {
            redisTemplate.opsForHash().delete(key, item);
        });
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return DS(() -> {
            return redisTemplate.opsForHash().hasKey(key, item);
        });
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        return DS(() -> {
            return redisTemplate.opsForHash().increment(key, item, by);
        });
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return DS(() -> {
            return redisTemplate.opsForHash().increment(key, item, -by);
        });
    }
    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<String> sGet(String key) {
        return DS(() -> {
            return redisTemplate.opsForSet().members(key);
        });
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        return DS(() -> {
            return redisTemplate.opsForSet().isMember(key, value);
        });
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, String... values) {
        return DS(() -> {
            return redisTemplate.opsForSet().add(key, values);
        });
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, String... values) {
        return DS(() -> {
            final Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        });
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSize(String key) {
        return DS(() -> {
            return redisTemplate.opsForSet().size(key);
        });
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long sRemove(String key, Object... values) {
        return DS(() -> {
            final Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        });
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束  0 到 -1代表所有值
     * @return
     */
    public List<String> lGet(String key, long start, long end) {
        return DS(() -> {
            return redisTemplate.opsForList().range(key, start, end);
        });
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        return DS(() -> {
            return redisTemplate.opsForList().size(key);
        });
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        return DS(() -> {
            return redisTemplate.opsForList().index(key, index);
        });
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, String value) {
        return DS(() -> {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        });
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, String value, long time) {
        return DS(() -> {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        });
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<String> value) {
        return DS(() -> {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        });
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<String> value, long time) {
        return DS(() -> {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        });
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, String value) {
        return DS(() -> {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        });
    }

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}