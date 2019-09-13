package com.xumou.demo.test.spring.database;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class SqlUtils {

    /** 将大写转为下划线加小写 */
    public static final ColumnCall UPPER_TO_LINE = column -> {
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
    public static final ColumnCall IGNORE_NULL = column -> {
        if (column.getFieldName() == null)
            column.setIgnore(true);
    };

    /** 忽略指定属性 */
    public static final ColumnCall IGNORE(String ... name){
        return column -> {
            for (String str : name) {
                if(column.fieldName.equals(str))
                    column.setIgnore(true);
            }
        };
    }

    public static BatchSqlObj batchUpdateSql(List list, String table, ColumnCall... columnMappers){
        return batchExecuteSql(list, table, fields -> {
            return updateSql(list.get(0), table, joinCol(columnMappers, call(column -> {
                fields.add(column.getFiled());
            })));
        }, columnMappers);
    }

    public static BatchSqlObj batchInsertSql(List list, String table, ColumnCall... columnMappers){
        return batchExecuteSql(list, table, fields -> {
            return insertSql(list.get(0), table, joinCol(columnMappers, call(column -> {
                fields.add(column.getFiled());
            })));
        }, columnMappers);
    }

    private static BatchSqlObj batchExecuteSql(List list, String table, BatchCall call, ColumnCall... columnMappers){
        isTree(list != null && !list.isEmpty(), "list not is null");
        List<Field> fields = new LinkedList<>();
        SqlObj sqlObj = call.call(fields);
        List<Object[]> batchArgs = new LinkedList<>();
        batchArgs.add(sqlObj.getArgs());
        for (int i = 1; i < list.size(); i++) {
            Object obj = list.get(i);
            List<Object> args = new LinkedList<>();
            for (Field field : fields) {
                try {
                    args.add(field.get(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            batchArgs.add(args.toArray());
        }
        return batchSqlObj(sqlObj.getSql(), batchArgs);
    }

    public static SqlObj updateByColSql(Object obj, String cols, ColumnCall ... columnCalls){
        Alias tableName = obj.getClass().getAnnotation(Alias.class);
        String table = "";
        if(tableName != null){
            table = tableName.value();
        }else{
            Column column = new Column(null, null);
            String name = obj.getClass().getSimpleName();
            char c = name.charAt(0);
            c = (char)(c>=65 && c<=90 ? c + 32 : c);
            column.setColumnName(c + name.substring(1));
            UPPER_TO_LINE.call(column);
            table = column.getColumnName();
        }
        return updateByColSql(obj, table, cols, columnCalls);
    }

    public static SqlObj updateByColSql(Object obj, String table, String cols, ColumnCall ... columnCalls){
        isTree(!empty(cols), "cols not is null");
        Set set = strToCollection(new HashSet(), cols, ",");
        return updateWhereSql(obj, table, call(columnCalls), call(column -> {
            if(set.contains(column.getFieldName())){
                column.setIgnore(false);
            }else{
                column.setIgnore(true);
            }
        }), UPPER_TO_LINE);
    }

    public static SqlObj updateWhereSql(Object obj, String table, ColumnCall[] columnCalls,
                                        ColumnCall[] whereCall, ColumnCall ... commonCall){
        SqlObj update = updateSql(obj, table, joinCol(commonCall, columnCalls));
        SqlObj where = whereSql(obj, joinCol(commonCall, whereCall));
        String sql = update.getSql() + (empty(where.getSql()) ? "" : "where (" + where.getSql() + ")");
        return sqlObj(sql, arrsToList(update.getArgs(), where.getArgs()));
    }

    public static SqlObj insertSql(Object obj, String table, ColumnCall... columnMappers){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sb.append("insert into ").append(table).append(" (");
        sql(obj, joinCol(columnMappers, call(column -> {
            if(column.ignore) return;
            sb.append(column.getColumnName()).append(", ");
            args.add(column.getFieldValue());
        })));
        delEnd(sb, ", ");
        sb.append(") values (");
        for (int i = 0; i < args.size(); i++) {
            sb.append("?, ");
        }
        delEnd(sb, ", ");
        sb.append(")");
        return sqlObj(sb.toString(), args);
    }

    public static SqlObj updateSql(Object obj, String table, ColumnCall... columnCalls){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sb.append("update ").append(table).append(" set ");
        sql(obj, joinCol(columnCalls, call(column -> {
            if(column.ignore) return;
            sb.append(column.getColumnName()).append("=").append("?, ");
            args.add(column.getFieldValue());
        })));
        delEnd(sb, ", ");
        endBlank(sb);
        return sqlObj(sb.toString(), args);
    }

    public static SqlObj whereSql(Object obj, ColumnCall... columnMappers){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sql(obj, joinCol(columnMappers, call(column -> {
            if(column.ignore) return;
            if(column.getAndSql() == null){
                sb.append("and ").append(column.getColumnName()).append("=").append("? ");
                args.add(column.getFieldValue());
            }else{
                sb.append(column.getAndSql());
                if(column.getAndSqlArgs() != null){
                    for (Object andSqlArg : column.getAndSqlArgs()) {
                        args.add(andSqlArg);
                    }
                }
                endBlank(sb);
            }
        })));
        delEnd(sb, ", ");
        delStart(sb,"and ");
        return sqlObj(sb.toString(), args);
    }

    public static List arrsToList(Object[] ... obj){
        List list = new LinkedList();
        for (Object[] objects : obj) {
            for (Object object : objects) {
                list.add(object);
            }
        }
        return list;
    }

    public static <T extends Collection> T strToCollection(T t, String str, String delim){
        StringTokenizer tokenizer = new StringTokenizer(str, delim);
        while(tokenizer.hasMoreElements()){
            t.add(tokenizer.nextElement());
        }
        return t;
    }

    public static ColumnCall[] joinCol(ColumnCall[] ... columnCalls){
        List<ColumnCall> list = new LinkedList<>();
        for (ColumnCall[] cs : columnCalls) {
            for (ColumnCall c : cs) {
                list.add(c);
            }
        }
        return list.toArray(new ColumnCall[]{});
    }

    public static ColumnCall[] call(ColumnCall ... calls){
        return calls;
    }

    public static void delEnd(StringBuilder sb, String str){
        if(sb != null && str != null && sb.length() > str.length()){
            String substring = sb.substring(sb.length() - str.length());
            if(str.equals(substring)){
                sb.delete(sb.length() - str.length(), sb.length());
            }
        }
    }

    public static void delStart(StringBuilder sb, String str){
        if(sb != null && str != null && sb.length() > str.length()){
            String substring = sb.substring(0, str.length());
            if(str.equals(substring)){
                sb.delete(0, str.length());
            }
        }

    }

    public static void endBlank(StringBuilder sb){
        if(sb.charAt(sb.length()-1) != ' '){
            sb.append(" ");
        }
    }

    public static void isTree(boolean boo, String msg) {
        if(!boo) throw new RuntimeException(msg);
    }

    public static boolean empty(String str){
        if(str != null && str.trim().length() > 0){
            return false;
        }else{
            return true;
        }
    }

    private static void sql(Object obj, ColumnCall... columnMappers){
        isTree(obj != null, "obj not null");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Column column = new Column(field, field.getName());
                column.setFieldValue(field.get(obj));
                Alias alias = field.getAnnotation(Alias.class);
                column.setColumnName(alias == null ? field.getName() : alias.value());
                for (ColumnCall columnMapper : columnMappers) {
                    columnMapper.call(column);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static SqlObj sqlObj(String sql, List args){
        SqlObj sqlObj = new SqlObj();
        sqlObj.setSql(sql);
        sqlObj.setArgs(args.toArray());
        return sqlObj;
    }

    private static BatchSqlObj batchSqlObj(String sql, List<Object[]> batchArgs) {
        BatchSqlObj batchSqlObj = new BatchSqlObj();
        batchSqlObj.setSql(sql);
        batchSqlObj.setBatchArgs(batchArgs);
        return batchSqlObj;
    }

    @Data
    public static class SqlObj{
        private String sql;
        private Object[] args;
        public int update(JdbcTemplate jdbcTemplate){
            return jdbcTemplate.update(sql, args);
        }
    }

    @Data
    public static class BatchSqlObj{
        private String sql;
        private List<Object[]> batchArgs;
        public int[] batchUpdate(JdbcTemplate jdbcTemplate){
            return jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    @Data
    public static class Column {
        private final Field filed;
        private final String fieldName;
        private Object fieldValue;
        private String columnName;
        private String andSql;
        private Object[] andSqlArgs;
        private boolean ignore;
    }

    @FunctionalInterface
    public static interface BatchCall {
        SqlObj call(List<Field> fields);
    }

    @FunctionalInterface
    public static interface ColumnCall {
        void call(Column column);
    }

    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Alias{
        String value();
    }
}
