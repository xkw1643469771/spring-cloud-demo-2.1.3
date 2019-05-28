package com.xumou.demo.web.session.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class TestController {

    @RequestMapping("get")
    public Object get(HttpSession session, String name){
        System.out.println(name);
        return session.getAttribute(name);
    }

    @RequestMapping("set")
    public Object set(HttpSession session, String name, String value){
        System.out.println(name+": "+value);
        session.setAttribute(name, value);
        return "success";
    }

}
