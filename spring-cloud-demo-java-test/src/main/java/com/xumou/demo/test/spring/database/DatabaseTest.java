package com.xumou.demo.test.spring.database;

import org.junit.Test;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DatabaseTest {

    public static final Random RANDOM = new Random();

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
                for (int j = 0; j < 100; j++) {
                    TblUser tblUser = new TblUser();
                    tblUser.setName(String.valueOf(Math.random()));
                    tblUser.setBirthday(new Date());
                    tblUser.setDate(new Date());
                    tblUser.setMoney(RANDOM.nextDouble());
                    tblUser.setPayMoney(new BigDecimal(RANDOM.nextInt(9999) + "." + RANDOM.nextInt(99)));
                    tblUser.setOrderNo(RANDOM.nextLong());
                    tblUser.setRealMoney(new BigDecimal(RANDOM.nextInt()));
                    tblUser.setTime(new Date());
                    list.add(tblUser);
                }
                SqlUtils.batchInsertSql(list, SqlUtils.IGNORE_NULL).batchUpdate(jdbcTemplate);
                System.out.println(i);
            }
        });
    }

    @Test
    public void batchUpdate(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser t1 = new TblUser();
            t1.setName(String.valueOf(Math.random()));
            TblUser t2 = new TblUser();
            t2.setName(String.valueOf(Math.random()));
            SqlUtils.BatchSqlObj batchSqlObj = SqlUtils.batchUpdateByColSql(Arrays.asList(t1, t2),
                    "column1", SqlUtils.BYCOL("column1"), SqlUtils.IGNORE_NULL);
            batchSqlObj(batchSqlObj);
            int[] ints = batchSqlObj.batchUpdate(jdbcTemplate);
            System.out.println(Arrays.toString(ints));
        });
    }

    @Test
    public void deleteWhere(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser t1 = new TblUser();
            t1.setName("123");
            t1.setOrderNo(123123L);
            int count = SqlUtils.deleteWhereSql(t1, SqlUtils.IGNORE_NULL).update(jdbcTemplate);
            System.out.println();
            System.out.println(count);
            System.out.println();
        });
    }

    @Test
    public void updateWhere(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser t1 = new TblUser();
            t1.setName("123");
            t1.setOrderNo(123123L);
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
            TblUser t1 = new TblUser();
            t1.setName("123");
            t1.setOrderNo(123123L);
            SqlUtils.SqlObj sqlObj = SqlUtils.deleteByColSql(t1, "column1");
            int update = sqlObj.update(jdbcTemplate);
            System.out.println(update);
            System.out.println(sqlObj);
        });
    }

    @Test
    public void whereTest(){
        TblUser t1 = new TblUser();
        t1.setName("123");
        t1.setOrderNo(123123L);
        SqlUtils.SqlObj sqlObj = SqlUtils.whereSql(t1, column -> {
            if(column.getFieldName().equals("columnTow") || column.getFieldName().equals("column3")){
                column.setExtSql("and # in (?,?,?,?,?,?,?,?)");
            }
            column.setExtSqlArgs(new String[]{"1","2","3","4","5","6","7","8"});
        });
        System.out.println(sqlObj);
    }

    @Test
    public void selectSql(){
        SqlUtils.SqlObj sqlObj = SqlUtils.selectSql(new TblUser());
        System.out.println(sqlObj);
    }

    @Test
    public void selectWhereSql(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser tab = new TblUser();
            tab.setId(22);
            List<TblUser> query = SqlUtils.selectSql(tab, SqlUtils.SEL_AS).query(jdbcTemplate, TblUser.class);
            for (TblUser tblUser : query) {
                System.out.println(tblUser);
            }
        });
    }

    @Test
    public void linkedSelect(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser tab = new TblUser();
            tab.setId(22);
            List<TblUser> list = SqlUtils.select(tab).where(tab).byCol("id").list(jdbcTemplate, TblUser.class);
            for (TblUser tblUser : list) {
                System.out.println(tblUser);
            }
        });
    }

    @Test
    public void linkedInsert(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser tab = new TblUser();
            tab.setName("12312312312321313");
            SqlUtils.insert(tab).update(jdbcTemplate);
        });
    }

    @Test
    public void linkedUpdate(){
        JdbcUtils.transction(jdbcTemplate -> {
            TblUser tab = new TblUser();
            tab.setName("345345");
            tab.setId(271716);
            SqlUtils.update(tab).byCol("name").where(tab).byCol("id").update(jdbcTemplate);
        });
    }

    @Test
    public void generatorOneTable(){
        String tableName = "TBL_USER";
        SqlUtils.setGeneratorDatabse("org.h2.Driver",
                "jdbc:h2:tcp://192.168.88.201:8082/~/test",
                "root", "root");
        String str = SqlUtils.generatorStr(tableName);
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(str), null);
        System.out.println(str);
        System.out.println("已经复制！");
    }

}
