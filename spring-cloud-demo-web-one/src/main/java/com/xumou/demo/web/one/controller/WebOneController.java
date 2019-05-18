package com.xumou.demo.web.one.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebOneController {

    @Value("${server.port}")
    String port;
    @Value("${test}")
    String test;

    @PostMapping("test")
    public Object test(){
        return "WEB-ONE: " + port;
    }

    @GetMapping("test2")
    public Object test2(){
        return test;
    }

}
