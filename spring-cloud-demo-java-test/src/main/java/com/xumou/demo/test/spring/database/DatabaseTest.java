package com.xumou.demo.test.spring.database;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DatabaseTest {

    public void batchSqlObj(SqlUtils.BatchSqlObj batchSqlObj){
        System.out.println();
        System.out.println(batchSqlObj.getSql());
        batchSqlObj.getBatchArgs().forEach(e-> System.out.println(Arrays.toString(e)));
        System.out.println();
    }

    @Test
    public void batchInsert(){
        JdbcUtils.transction(jdbcTemplate -> {
            for (int i = 0; i < 100; i++) {
                List list = new ArrayList();
                for (int j = 0; j < 10000; j++) {
                    TblTest tblTest = new TblTest();
                    tblTest.setColumn1(1);
                    tblTest.setColumnTow(String.valueOf(Math.random()));
                    tblTest.setColumn3(new Date());
                    list.add(tblTest);
                }
                int[] ints = SqlUtils.batchInsertSql(list).batchUpdate(jdbcTemplate);
                System.out.println(ints);
            }
        });
    }

    @Test
    public void batchUpdate(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblTest t1 = new TblTest();
            t1.setColumn1(123);
            t1.setColumnTow("123123");
            TblTest t2 = new TblTest();
            t2.setColumn3(new Date());
            SqlUtils.BatchSqlObj batchSqlObj = SqlUtils.batchUpdateByColSql(Arrays.asList(t1, t2),
                    "column1", SqlUtils.IGNORE("column1"), SqlUtils.IGNORE_NULL);
            batchSqlObj(batchSqlObj);
            int[] ints = batchSqlObj.batchUpdate(jdbcTemplate);
            System.out.println(Arrays.toString(ints));
        });
    }

    @Test
    public void deleteWhere(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblTest t1 = new TblTest();
            t1.setColumn1(123);
            t1.setColumnTow("123123");
            int count = SqlUtils.deleteWhereSql(t1, SqlUtils.IGNORE_NULL).update(jdbcTemplate);
            System.out.println();
            System.out.println(count);
            System.out.println();
        });
    }

    @Test
    public void updateWhere(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblTest t1 = new TblTest();
            t1.setColumn1(123);
            t1.setColumnTow("123123");
            int count = SqlUtils.updateWhereSql(t1, SqlUtils.join(column -> {

            }), SqlUtils.join(column -> {

            }), SqlUtils.IGNORE_NULL).update(jdbcTemplate);
            System.out.println();
            System.out.println(count);
            System.out.println();
        });
    }

    @Test
    public void deleteByCol(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblTest t1 = new TblTest();
            t1.setColumn1(123);
            t1.setColumnTow("123123");
            SqlUtils.SqlObj sqlObj = SqlUtils.deleteByColSql(t1, "column1");
            int update = sqlObj.update(jdbcTemplate);
            System.out.println(update);
            System.out.println(sqlObj);
        });
    }

    @Test
    public void generatorStr(){
        SqlUtils.setGeneratorDatabse("org.h2.Driver",
                "jdbc:h2:tcp://192.168.88.201:8082/~/test", "root", "root");
        String str = SqlUtils.generatorStr("TBL_USER");
        System.out.println(str);
    }

}
