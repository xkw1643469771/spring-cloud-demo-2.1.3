package com.xumou.demo.lock.redis.controller;

import com.xumou.demo.lock.redis.utils.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RestController
public class LockRedisController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("lock")
    public Object lock(){
        RedisLock.lock("test");
        RedisLock.lock("test");
        RedisLock.lock("test");

        RedisLock.unlock("test");
        RedisLock.unlock("test");
        RedisLock.unlock("test");
        return "SUCCESS";
    }

}
