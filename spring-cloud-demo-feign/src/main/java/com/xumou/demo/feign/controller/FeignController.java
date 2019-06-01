package com.xumou.demo.feign.controller;

import com.xumou.demo.feign.client.WebOneFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeignController {

    @Autowired
    WebOneFeign webOneFeign;

    @GetMapping("test")
    public Object test(){
        return webOneFeign.test();
    }

    @GetMapping("test2")
    public Object test2(){
        return "SUCCESS";
    }


}
