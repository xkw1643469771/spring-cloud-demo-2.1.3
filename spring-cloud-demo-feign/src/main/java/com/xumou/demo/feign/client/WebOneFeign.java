package com.xumou.demo.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "web-one")
public interface WebOneFeign {

    @PostMapping("test")
    public String test();

}
