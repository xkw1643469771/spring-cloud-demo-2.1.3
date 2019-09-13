package com.xumou.demo.test.spring.database;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatabaseTest {

    @Test
    public void jdbcTest(){
        JdbcUtils.transction(jdbcTemplate -> {
            TestObj test = new TestObj();
            test.setName("Tom");
            test.setNameStr("123123");
            SqlUtils.insertSql(test, "test", SqlUtils.IGNORE_NULL, SqlUtils.UPPER_TO_LINE, SqlUtils.IGNORE("nameStr"))
                    .update(jdbcTemplate);
            List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from test");
            System.out.println(maps);
            throw new Exception();
        });
    }

    @Test
    public void sqlTest(){
        TestObj test = new TestObj();
        test.setName("Tom");
        test.setNameStr("123123");
        SqlUtils.SqlObj sqlObj = SqlUtils.updateSql(test, "test", column ->  {

        });
        System.out.println(sqlObj);
    }

    @Test
    public void batchSqlTest(){
        TestObj test = new TestObj();
        test.setName("Tom");
        test.setNameStr("123123");
        SqlUtils.BatchSqlObj sqlObj = SqlUtils.batchUpdateSql( Arrays.asList(test,test), "test",
                SqlUtils.IGNORE_NULL, SqlUtils.UPPER_TO_LINE, SqlUtils.IGNORE("nameStr"));
        System.out.println(sqlObj);
    }

    @Test
    public void whereSqlTest(){
        TestObj test = new TestObj();
        test.setName("Tom");
        test.setNameStr("123123");
        SqlUtils.SqlObj sqlObj = SqlUtils.updateSql( test, "test", SqlUtils.IGNORE("nameStr"));
        System.out.println(sqlObj);
        sqlObj = SqlUtils.whereSql( test, SqlUtils.IGNORE("nameStr"), column -> {
            if(column.getFieldName().equals("id")){
                column.setAndSql("and (id > ? and id < ? )");
                column.setAndSqlArgs(Arrays.asList(1,2).toArray());
            }
        });
        System.out.println(sqlObj);
    }

    @Test
    public void updateWhereSqlTest(){
        TestObj test = new TestObj();
        test.setName("Tom");
        test.setNameStr("123123");
        SqlUtils.SqlObj test1 = SqlUtils.updateWhereSql(test, "test",
                SqlUtils.call(column -> {
                    if(column.getFieldName().equals("id")){
                        column.setIgnore(true);
                    }else{
                        column.setIgnore(false);
                    }
                }),
                SqlUtils.call(column -> {
                    if(!column.getFieldName().equals("id")){
                        column.setIgnore(true);
                    }else{
                        column.setIgnore(false);
                    }
                }), SqlUtils.UPPER_TO_LINE);
        System.out.println(test1);
    }

    @Test
    public void updateByIdSql(){
        TestObj test = new TestObj();
        test.setName("Tom");
        test.setNameStr("123123");
        SqlUtils.updateByColSql(test, "id", SqlUtils.IGNORE("id")).update(null);
    }

    @Test
    public void generatorStr(){
        SqlUtils.setGeneratorDatabse("org.h2.Driver",
                "jdbc:h2:tcp://192.168.88.201:8082/~/test", "root", "r");
        String str = SqlUtils.generatorStr("TBL_TEST");
        System.out.println(str);
    }

}
