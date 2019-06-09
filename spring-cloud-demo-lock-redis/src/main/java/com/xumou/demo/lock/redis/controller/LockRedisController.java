package com.xumou.demo.lock.redis.controller;

import com.xumou.demo.lock.redis.utils.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class LockRedisController {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Random random = new Random();
    long sumTimer;
    int count;

    @GetMapping("lock")
    public Object lock(Long startTimer){
        RedisLock.lock("test", 1000);
        int timer = random.nextInt(1);
        RedisLock.sleep(timer);
        sumTimer += timer;
        long long2 = System.currentTimeMillis() - startTimer;
        logger.info("执行id：" + count++ + "\t\t本次等待：" + timer + "\t\t总等待：" + sumTimer +"\t\t经过时间：" + long2 + "\t\t时间差：" + (long2 - sumTimer));
        RedisLock.unlock("test");
        return "SUCCESS";
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        ExecutorService es = Executors.newFixedThreadPool(100);
        RestTemplate restTemplate = new RestTemplate();
        for (int i = 0; i < 10000; i++) {
            final int idx = i;
            es.execute(() -> {
                restTemplate.getForEntity("http://192.168.1.103:8959/lock?startTimer=" + time, String.class);
            });
        }
    }

}
