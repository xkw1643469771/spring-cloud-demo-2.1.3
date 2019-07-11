package com.xumou.demo.mybatis.plug.controller;

import com.xumou.demo.mybatis.plug.po.Test;
import com.xumou.demo.mybatis.plug.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    public TestService testService;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @GetMapping("test")
    public Object test(){
        return testService.test();
    }

    @GetMapping("test2")
    public Object test2(){
        String sql = "select id from tbl_test";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<Test>());
    }

    @GetMapping("test3")
    public Object test3(){
        String sql = "select id from tbl_test where id = 1139180610938556434";
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("test4")
    public Object test4(){
        return testService.test4();
    }

}
