package com.xumou.demo.test.spring.database;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

public class JdbcUtils {

    public static final String CLASS_NAME = "org.h2.Driver";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "r";
    public static final String JDBC_URL = "jdbc:h2:tcp://192.168.88.201:8082/~/test";

    private static class Init{
        static JdbcTemplate jdbcTemplate;
        static PlatformTransactionManager transactionManager;
        static {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setDriverClassName(CLASS_NAME);
            dataSource.setJdbcUrl(JDBC_URL);
            dataSource.setUsername(USERNAME);
            dataSource.setPassword(PASSWORD);
            transactionManager = new DataSourceTransactionManager(dataSource);
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
    }

    @FunctionalInterface
    public static interface TransCall{
        void call(JdbcTemplate jdbcTemplate) throws Exception;
    }

    @FunctionalInterface
    public static interface TransCallReturn{
        Object call(JdbcTemplate jdbcTemplate) throws Exception;
    }

    public static void transction(TransCall call){
        transctionReturn(jdbcTemplate -> {
            call.call(jdbcTemplate);
            return null;
        });
    }

    public static <T> T transctionReturn(TransCallReturn call){
        TransactionStatus status = null;
        try{
            status = Init.transactionManager.getTransaction(new DefaultTransactionAttribute());
            Object result = call.call(Init.jdbcTemplate);
            Init.transactionManager.commit(status);
            return (T) result;
        }catch (Exception e){
            Init.transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

}
