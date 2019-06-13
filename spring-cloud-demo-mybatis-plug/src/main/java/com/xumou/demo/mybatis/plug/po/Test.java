package com.xumou.demo.mybatis.plug.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("tbl_test")
public class Test {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private LocalDate birthday;

    private String username;

    private String password;


}
