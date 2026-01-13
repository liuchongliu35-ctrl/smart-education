package com.bing.tpa.utils.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings(value = {"unchecked","rawtypes"})
@Component
public class RedisCache {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 缓存基本对象，integer，string 实体类等
     * **/
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key,value);
    }

    public <T> void setCacheObject(final String key, final T value, final Integer timeout,final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key,value,timeout,timeUnit);
    }

    public boolean expire(final String key, final long timeout){
        return expire(key,timeout,TimeUnit.SECONDS);
    }

    public boolean expire(final String key, final long timeout,final TimeUnit timeUnit){
        return redisTemplate.expire(key,timeout,timeUnit);
    }

    public <T> T getCacheObject(final String key) {
        ValueOperations<String ,T > operations=redisTemplate.opsForValue();
        return operations.get(key);
    }

    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     * **/
    public long deleteObject(final Collection collection){
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     */
    public <T> long setCacheList(final String key,final List<T> dataList){
        Long count=redisTemplate.opsForList().rightPushAll(key,dataList);
        return count==null?0:count;
    }

    /**
     * 获取缓存的List对象
     */
    public <T> List<T> getCacheList(final String key){
        return redisTemplate.opsForList().range(key,0,-1);
    }

    /**
     * 缓存set
     */

    public <T> BoundSetOperations<String,T> setChacheSet(final String key,final Set<T> dataSet){
        BoundSetOperations<String ,T> setOperations=redisTemplate.boundSetOps(key);
        Iterator<T> it=dataSet.iterator();
        while(it.hasNext()){
            setOperations.add(it.next());
        }
        return setOperations;
    }

    /**
     * 获取set
     */
    public <T> Set<T> getCacheSet(final String key){
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存MAp
     */
    public <T> void setCacheMap(final String key,final Map<String,T> dataMap){
        if(dataMap!=null){
            redisTemplate.opsForHash().putAll(key,dataMap);
        }
    }

    /**
     * 获取Map
     */
    public <T> Map<String,T> getCacheMap(final String key){
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key
     * @param hkey
     */
    public void delCacheMapValue(final String key, final String hkey)
    {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hkey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }



}
