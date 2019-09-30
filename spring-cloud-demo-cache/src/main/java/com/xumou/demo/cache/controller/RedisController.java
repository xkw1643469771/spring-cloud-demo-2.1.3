package com.xumou.demo.cache.controller;

import com.xumou.demo.cache.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @GetMapping("redisTest1")
    public void redisTest1(){
        User user = new User();
        redisTemplate.opsForValue().set("user", user);
    }

}
