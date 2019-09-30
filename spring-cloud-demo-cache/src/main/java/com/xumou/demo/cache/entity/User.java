package com.xumou.demo.cache.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class User {

    private Long id;
    private String name;
    private Date date;
    private BigDecimal money;

}
