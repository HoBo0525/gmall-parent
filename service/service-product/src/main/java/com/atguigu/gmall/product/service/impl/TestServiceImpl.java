package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.product.service.TestService;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Hobo
 * @create 2021-02-06 20:32
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;


    @Override
    public  void testLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 2, TimeUnit.SECONDS);
        if (flag){

            String num = redisTemplate.opsForValue().get("num");

            if (StringUtils.isBlank(num)){
                return;
            }

            int i = Integer.parseInt(num);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++i));


            //lua脚本
            String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);

            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);

//            if (uuid.equals(this.redisTemplate.opsForValue().get("lock"))){
//                //lock 过期
//                this.redisTemplate.delete("lock");
//            }
        }else {
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
