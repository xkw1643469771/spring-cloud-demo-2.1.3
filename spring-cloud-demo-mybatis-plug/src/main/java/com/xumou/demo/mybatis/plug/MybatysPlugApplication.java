package com.xumou.demo.mybatis.plug;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
@MapperScan({"com.baomidou.mybatisplus.samples.quickstart.mapper", "com.xumou.demo.mybatis.plug"})
public class MybatysPlugApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatysPlugApplication.class, args);
    }

}
