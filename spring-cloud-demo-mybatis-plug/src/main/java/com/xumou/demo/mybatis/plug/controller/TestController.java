package com.xumou.demo.mybatis.plug.controller;

import com.xumou.demo.mybatis.plug.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    public TestService testService;

    @GetMapping("test")
    public Object test(){
        return testService.test();
    }

}
