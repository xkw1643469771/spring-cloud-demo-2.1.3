package com.xumou.demo.test.spring.database;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DatabaseTest {

    @Test
    public void jdbcTest(){
        JdbcUtils.transction(jdbcTemplate -> {
            TestObj test = new TestObj();
            test.setName("Tom");
            test.setNameStr("123123");
            JdbcUtils.SqlObj sqlObj = JdbcUtils.insertSql(test, "test", JdbcUtils.IGNORE_NULL, JdbcUtils.UPPER_TO_LINE, JdbcUtils.IGNORE("name_str"));
            jdbcTemplate.update(sqlObj.getSql(), sqlObj.getArgs());
            List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from test");
            System.out.println(maps);
            throw new Exception();
        });
    }

}
