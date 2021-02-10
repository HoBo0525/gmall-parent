package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Hobo
 * @create 2021-02-09 21:21
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint){
        //声明一个对象
        Object object = new Object();

        //在环绕通知中处理业务逻辑、 获取注解 注解使用在方法上
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //获取注解前缀
        String prefix = gmallCache.prefix();
        //注解使用在方法上的参数
        Object[] args = joinPoint.getArgs();
        //组成缓存的key
        String key = prefix + Arrays.asList(args).toString();

        try {
            //从redis获取数据
            object = cacheHit(key, signature);
            //无缓存
            if (object == null){
                //上锁
                String lockKey = prefix + ":lock";
                RLock lock = redissonClient.getLock(lockKey);
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (flag){
                    try {
                        //执行方法体
                        object = joinPoint.proceed(joinPoint.getArgs());
                        if (object == null){    //防止缓存穿透
                            Object objectNull = new Object();
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(objectNull),RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return objectNull;
                        }
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(object),RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return object;
                    } finally {
                        //解锁
                        lock.unlock();
                    }

                }else {
                    //自旋
                    Thread.sleep(100);
                    return cacheAroundAdvice(joinPoint);
                }

            }else {
                //从redis获取数据
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //数据库保底
        return joinPoint.proceed(joinPoint.getArgs());

    }


    private Object cacheHit(String key, MethodSignature signature) {
        //通过key   获取数据
        String strJson = (String) redisTemplate.opsForValue().get(key);
        if (strJson != null){
            //获取使用注解方法的返回值类型
            Class returnType = signature.getReturnType();
            //把redis获取的str 转化为使用注解方法的返回值类型
            Object object = JSON.parseObject(strJson, returnType);
            return object;
        }
        return null;
    }
}
