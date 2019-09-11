package com.xumou.demo.test.spring.database;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class JdbcUtils {

    public static final String CLASS_NAME = "org.h2.Driver";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    public static final String JDBC_URL = "jdbc:h2:d:/h2/test";

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

    // Sql相关操作  =====================================================================================================

    /** 将大写转为下划线加小写 */
    public static final ColumnMapper UPPER_TO_LINE = column -> {
        char[] cs = column.getColumnName().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : cs) {
            if(c >= 65 && c <= 90){
                sb.append("_");
                c = (char)(c + 32);
            }
            sb.append(c);
        }
        column.setColumnName(sb.toString());
    };

    /** 忽略为空的属性 */
    public static final ColumnMapper IGNORE_NULL = column -> {
        if (column.getColumnValue() == null)
            column.setIgnore(true);
    };

    /** 忽略指定属性 */
    public static final ColumnMapper IGNORE(String ... name){
        return column -> {
            for (String str : name) {
                if(column.columnName.equals(str))
                    column.setIgnore(true);
            }
        };
    }

    public static SqlObj updateSql(Object obj, String table, ColumnMapper... columnMappers){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sb.append("update ").append(table).append(" set ");
        sql(obj, column -> {
            sb.append(column.getColumnName()).append("=").append("?, ");
            args.add(column.getColumnValue());
        }, columnMappers);
        delEnd(sb, ", ");
        return sqlObj(sb.toString(), args);
    }

    public static SqlObj insertSql(Object obj, String table, ColumnMapper... columnMappers){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sb.append("insert into ").append(table).append(" (");
        sql(obj, column -> {
            sb.append(column.getColumnName()).append(", ");
            args.add(column.getColumnValue());
        }, columnMappers);
        delEnd(sb, ", ");
        sb.append(") values (");
        for (int i = 0; i < args.size(); i++) {
            sb.append("?, ");
        }
        delEnd(sb, ", ");
        sb.append(")");
        return sqlObj(sb.toString(), args);
    }

    private static SqlObj sqlObj(String sql, List args){
        SqlObj sqlObj = new SqlObj();
        sqlObj.setSql(sql);
        sqlObj.setArgs(args.toArray());
        return sqlObj;
    }

    private static void delEnd(StringBuilder sb, String str){
        String substring = sb.substring(sb.length() - str.length());
        if(str.equals(substring)){
            sb.delete(sb.length() - str.length(), sb.length());
        }
    }

    private static void sql(Object obj, SqlMapper sqlMapper, ColumnMapper... columnMappers){
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Column column = new Column();
                column.setColumnName(field.getName());
                column.setColumnValue(field.get(obj));
                for (ColumnMapper columnMapper : columnMappers) {
                    columnMapper.exec(column);
                }
                if(column.ignore){
                    continue;
                }
                sqlMapper.exec(column);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Data
    public static class SqlObj{
        private String sql;
        private Object[] args;
    }

    @Data
    public static class Column {
        private String columnName;
        private Object columnValue;
        private boolean ignore;
    }

    @FunctionalInterface
    public static interface ColumnMapper {
        void exec(Column column);
    }

    @FunctionalInterface
    public static interface SqlMapper {
        void exec(Column column);
    }
}
