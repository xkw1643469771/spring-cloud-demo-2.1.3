package com.xumou.demo.cache.controller;

import com.xumou.demo.cache.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

@RestController
public class CacheController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // 测试Redis
    @GetMapping("test")
    public void test(){
        stringRedisTemplate.opsForValue().set("a", "1");
    }

    // 测试缓存
    @GetMapping("test2")
    @Cacheable(cacheNames = "test2", key = "#key")
    public Object test2(String key){
        return Math.random();
    }

    // 测试对象
    @GetMapping("test3")
    @Cacheable(cacheNames = "test3", key = "#key")
    public User test3(String key){
        User user = new User();
        user.setDate(new Date());
        user.setId(System.currentTimeMillis());
        user.setMoney(BigDecimal.valueOf(Math.random()*10000));
        user.setName(String.valueOf((int)(Math.random()*1000)));
        return user;
    }
}
