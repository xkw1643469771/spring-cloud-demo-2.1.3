package com.xumou.demo.lock.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class LockRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockRedisApplication.class, args);
    }

}


