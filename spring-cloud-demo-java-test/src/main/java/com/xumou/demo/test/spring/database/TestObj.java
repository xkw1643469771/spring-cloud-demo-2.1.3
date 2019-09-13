package com.xumou.demo.test.spring.database;

import lombok.Data;

@Data
@SqlUtils.Alias("test")
public class TestObj {

    private Integer id;
    private String name;
    private String nameStr;

}
