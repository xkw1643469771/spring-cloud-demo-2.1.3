package com.xumou.demo.test.spring.database;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class SqlUtils {

    /** 忽略为空的属性 */
    public static final ColumnCall IGNORE_NULL = column -> {
        if (column.getFieldValue() == null){
            column.setIgnore(true);
        }
    };

    /** 查询别名, 对应字段名 */
    public static final ColumnCall SEL_AS = column -> {
        column.setColumnName(column.columnName + " as " + column.fieldName);
    };

    /** 指定字段 */
    public static final ColumnCall BYCOL(String fieldNames){
        isTree(!empty(fieldNames), "fieldNames not is null");
        Set<String> set = strToSet(fieldNames, ",");
        return column -> {
            if(set.contains(column.getFieldName())){
                column.setIgnore(false);
            }else{
                column.setIgnore(true);
            }
        };
    }

    /** 通过指定字段批量更新 */
    public static BatchSqlObj batchUpdateByColSql(List list, String fieldNames, ColumnCall... columnCalls){
        return batchExecuteSql(list, fields -> {
            ColumnCall call = column -> {
                if(column.ignore) return;
                fields.add(column.getFiled());
            };
            return updateWhereSql(list.get(0), joins(columnCalls, join(call)), join(BYCOL(fieldNames), call));
        });
    }

    /** 通过指定字段更新 */
    public static SqlObj updateByColSql(Object obj, String fieldNames, ColumnCall ... columnCalls){
        return updateWhereSql(obj, columnCalls, join(BYCOL(fieldNames)));
    }

    /** 通过条件更新 */
    public static SqlObj updateWhereSql(Object obj, ColumnCall[] columnCalls, ColumnCall[] whereCalls, ColumnCall ... commonCall){
        SqlObj update = updateSql(obj, joins(commonCall, columnCalls));
        SqlObj where = whereSql(obj, joins(commonCall, whereCalls));
        String sql = update.getSql() + "where " + where.getSql();
        return sqlObj(sql, arrsToList(update.getArgs(), where.getArgs()));
    }

    /** 更新一条 */
    public static SqlObj updateSql(Object obj, ColumnCall... columnCalls){
        return updateSql(obj, tableName(obj), columnCalls);
    }

    /** 批量插入 */
    public static BatchSqlObj batchInsertSql(List list, ColumnCall ... columnMappers){
        return batchExecuteSql(list, fields -> {
            return insertSql(list.get(0), joins(columnMappers, join(column -> {
                if(column.ignore) return;
                fields.add(column.getFiled());
            })));
        });
    }

    /** 插入一条 */
    public static SqlObj insertSql(Object obj, ColumnCall... columnMappers){
        return insertSql(obj, tableName(obj), columnMappers);
    }

    /** 根据指定字段删除 */
    public static SqlObj deleteByColSql(Object obj, String filedNames, ColumnCall ... whereCalls){
        return deleteWhereSql(obj, joins(join(BYCOL(filedNames)), whereCalls));
    }

    /** 根据条件删除 */
    public static SqlObj deleteWhereSql(Object obj, ColumnCall ... whereCalls){
        return deleteWhereSql(obj, tableName(obj), whereCalls);
    }

    /** 根据指定字段查询 */
    public static SqlObj selectWhereByColSql(Object obj, String whereFieldNames, ColumnCall ... commonCall){
        return selectWhereSql(obj, join(), join(BYCOL(whereFieldNames)), commonCall);
    }

    /** 根据条件查询 */
    public static SqlObj selectWhereSql(Object obj, ColumnCall[] columnCalls, ColumnCall[] whereCalls, ColumnCall ... commonCall){
        SqlObj selectSql = selectSql(obj, joins(commonCall, columnCalls));
        SqlObj whereSql = whereSql(obj, joins(commonCall, whereCalls));
        String sql = selectSql.getSql() + (empty(whereSql.getSql()) ? "" : "where " + whereSql.getSql());
        return sqlObj(sql, arrsToList(whereSql.getArgs()));
    }

    /** 查询所有 */
    public static SqlObj selectSql(Object obj, ColumnCall ... whereCalls){
        return selectSql(obj,tableName(obj), whereCalls);
    }

    private static BatchSqlObj batchExecuteSql(List list, BatchCall call){
        isTree(list != null && !list.isEmpty(), "list not is null");
        List<Field> fields = new LinkedList<>();
        SqlObj sqlObj = call.call(fields);
        List<Object[]> batchArgs = new LinkedList<>();
        batchArgs.add(sqlObj.getArgs());
        for (int i = 1; i < list.size(); i++) {
            Object obj = list.get(i);
            List<Object> args = new LinkedList<>();
            for (Field field : fields) {
                args.add(fieldValue(field, obj));
            }
            batchArgs.add(args.toArray());
        }
        return batchSqlObj(sqlObj.getSql(), batchArgs);
    }

    /** 插入一条, 自己指定表名 */
    public static SqlObj insertSql(Object obj, String tableName, ColumnCall... columnMappers){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sb.append("insert into ").append(tableName).append(" (");
        sql(obj, joins(columnMappers, join(column -> {
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

    /** 更新一条， 自己指定表名 */
    public static SqlObj updateSql(Object obj, String tableName, ColumnCall... columnCalls){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sb.append("update ").append(tableName).append(" set ");
        sql(obj, joins(columnCalls, join(column -> {
            if(column.ignore) return;
            sb.append(column.getColumnName()).append("=").append("?, ");
            args.add(column.getFieldValue());
        })));
        delEnd(sb, ", ");
        endBlank(sb);
        return sqlObj(sb.toString(), args);
    }

    /** 根据条件删除 */
    public static SqlObj deleteWhereSql(Object obj, String tableName, ColumnCall ... whereCalls){
        SqlObj sqlObj = whereSql(obj, whereCalls);
        sqlObj.setSql("delete from " + tableName + " where " + sqlObj.sql);
        return sqlObj;
    }

    /** 查询所有，自己指定表名 */
    public static SqlObj selectSql(Object obj, String tableName, ColumnCall ... columnCalls){
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sql(obj, joins(columnCalls, join(column -> {
            if(column.ignore) return;
            sb.append(column.getColumnName()).append(", ");
        })));
        delEnd(sb, ", ");
        endBlank(sb);
        sb.append("from ").append(tableName);
        endBlank(sb);
        return sqlObj(sb.toString(), Arrays.asList());
    }

    /** 条件拼接 */
    public static SqlObj whereSql(Object obj, ColumnCall ... columnCalls){
        StringBuilder sb = new StringBuilder();
        List<Object> args = new LinkedList<>();
        sql(obj, joins(columnCalls, join(column -> {
            if(column.ignore) return;
            if(column.extSql == null){
                sb.append("and ").append(column.getColumnName()).append("=").append("? ");
                args.add(column.getFieldValue());
            }else{
                sb.append(column.getExtSql().replaceAll("#", column.getColumnName()));
                if(column.extSqlArgs != null) args.addAll(Arrays.asList(column.extSqlArgs));
                endBlank(sb);
            }
        })));
        delEnd(sb, ", ");
        delStart(sb,"and ");
        String sql = sb.toString();
        sql = empty(sql.trim()) ? "" : "(" + sb + ")";
        return sqlObj(sql, args);
    }

    private static void sql(Object obj, ColumnCall... columnMappers){
        isTree(obj != null, "obj not null");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Column column = new Column(field, field.getName());
            column.setFieldValue(fieldValue(field, obj));
            Alias alias = field.getAnnotation(Alias.class);
            column.setColumnName(alias == null ? toColumnName(field.getName()) : alias.value());
            for (ColumnCall columnMapper : columnMappers) {
                columnMapper.call(column);
            }
        }
    }

    // =================================================================================================================

    public static Object classObj(Class c){
        try {
            return c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String tableName(Object obj) {
        isTree(obj != null, "obj not is null");
        Alias alias = obj.getClass().getAnnotation(Alias.class);
        if(alias != null){
            return alias.value();
        }else{
            String name = obj.getClass().getSimpleName();
            char c = name.charAt(0);
            c = (char)(c>=65 && c<=90 ? c + 32 : c);
            return c + toColumnName(name.substring(1));
        }
    }

    public static String toColumnName(String name){
        char[] cs = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : cs) {
            if(c >= 65 && c <= 90){
                sb.append("_");
                c = (char)(c + 32);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static Object fieldValue(Field field, Object obj){
        try{
            return field.get(obj);
        }catch (Exception e){
            return null;
        }
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

    public static Set strToSet(String str, String delim){
        Set set = new LinkedHashSet();
        StringTokenizer tokenizer = new StringTokenizer(str, delim);
        while(tokenizer.hasMoreElements()){
            set.add(tokenizer.nextElement());
        }
        return set;
    }

    public static ColumnCall[] joins(ColumnCall[] ... columnCalls){
        List<ColumnCall> list = new LinkedList<>();
        for (ColumnCall[] cs : columnCalls) {
            for (ColumnCall c : cs) {
                list.add(c);
            }
        }
        return list.toArray(new ColumnCall[]{});
    }

    public static ColumnCall[] join(ColumnCall ... calls){
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

        public <T> List<T> query(JdbcTemplate jdbcTemplate, Class<T> c){
            return jdbcTemplate.query(sql, args, (rs, rowNum) -> {
                Object o = classObj(c);
                sql(o, column -> {
                    try {
                        Object val = rs.getObject(column.getColumnName(), column.getFiled().getType());
                        column.getFiled().set(o, val);
                    } catch (Exception e) {
                        try {
                            Object val = rs.getObject(column.getFieldName(), column.getFiled().getType());
                            column.getFiled().set(o, val);
                        } catch (Exception e2) {
                            System.err.println(e2.getMessage());
                        }
                    }
                });
                return (T) o;
            });
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
        private String extSql;
        private Object[] extSqlArgs;
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

    // =================================================================================================================

    public static String generatorStr(String table){
        return Generator.classString(table);
    }

    public static void setGeneratorDatabse(String className, String jdbcUrl, String username, String password){
        Generator.className = className;
        Generator.jdbcUrl = jdbcUrl;
        Generator.username = username;
        Generator.password = password;
        Generator.init();
    }

    private static class Generator{

        static final String TABLE_COMMON = "";

        static String className;
        static String username;
        static String password;
        static String jdbcUrl;
        static Connection connection;
        static Map<String, Class> typeMap;

        static {
            typeMap = new LinkedHashMap<>();
            typeMap.put("INTEGER", Integer.class);
            typeMap.put("BIGINT", Long.class);

            typeMap.put("BOOLEAN", Boolean.class);

            typeMap.put("DOUBLE", Double.class);
            typeMap.put("DECIMAL", BigDecimal.class);

            typeMap.put("VARCHAR", String.class);

            typeMap.put("DATE", Date.class);
            typeMap.put("TIME", Date.class);
            typeMap.put("TIMESTAMP", Date.class);
        }

        static void init(){
            try {
                Class.forName(className);
                connection = DriverManager.getConnection(jdbcUrl, username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        static String classString(String tableName){
            Table table = tableCol(tableName);
            StringBuilder sb = new StringBuilder();
            Set<Class> set = new HashSet<>();
            set.add(SqlUtils.class);
            set.add(Data.class);
            line(sb, "/**");
            line(sb, " * ", table.getTableComments());
            line(sb, " */");
            line(sb, "@Data");
            line(sb, "@SqlUtils.Alias(\"", table.getTableName(), "\")");
            line(sb, "public class " ,className(table.getTableName()) ," {");
            for (Col col : table.getCols()) {
                line(sb, "\t/** ");
                line(sb, "\t * ", col.getColComments());
                line(sb, "\t */");
                line(sb, "\t@SqlUtils.Alias(\"", col.getColName(), "\")");
                line(sb, "\tprivate ", filedType(set, col.getColType()) ," " ,fieldName(col.getColName()), ";");
            }
            line(sb, "}");
            StringBuilder im = new StringBuilder();
            for (Class aClass : set) {
                line(im, "import ", aClass.getName(), ";");
            }
            line(im);
            return im.append(sb).toString();
        }

        static String className(String str){
            char c = str.toUpperCase().charAt(0);
            return c + fieldName(str.substring(1));
        }

        static String filedType(Set<Class> set, String str){
            Class s = typeMap.get(str);
            if(s == null){
                return str;
            }else{
                if(!s.getName().startsWith("java.lang.")){
                    set.add(s);
                }
                return s.getSimpleName();
            }
        }

        static String fieldName(String str){
            char[] cs = str.toLowerCase().toCharArray();
            boolean flag = false;
            StringBuilder sb = new StringBuilder();
            for (char c : cs) {
                if(c == '_'){
                    flag = true;
                }else if(flag){
                    if(c >= 'a' && c <= 'z'){
                        sb.append((char)(c-32));
                    }else{
                        sb.append(c);
                    }
                    flag = false;
                }else{
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        static void line(StringBuilder sb, Object ... obj){
            for (Object o : obj) {
                sb.append(o);
            }
            sb.append("\n");
        }

        static Table tableCol(String tableName){
            Table table = new Table();
            try{
                PreparedStatement ps = connection.prepareStatement("select * from " + tableName + " where 1 = 2 ");
                ResultSetMetaData metaData = ps.getMetaData();
                tableName = metaData.getTableName(1);
                Map<String, String> comments = comments(tableName);
                table.setTableName(tableName);
                table.setTableComments(comments.get(TABLE_COMMON));
                table.setCols(new LinkedList<>());
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    Col col = new Col();
                    col.setColName(metaData.getColumnName(i));
                    col.setColType(metaData.getColumnTypeName(i));
                    col.setColComments(comments.get(metaData.getColumnName(i)));
                    table.getCols().add(col);
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            return table;
        }

        static Map<String,String> comments(String table){
            if("org.h2.Driver".equals(className)){
                return query(new StringBuilder()
                        .append("select COLUMN_NAME, REMARKS from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = ? ")
                        .append("union ")
                        .append("select ?, REMARKS from INFORMATION_SCHEMA.TABLES where TABLE_NAME = ?")
                        .toString(), table, TABLE_COMMON, table
                );
            }else if("org.h2.Driver".equals(className)){

            }else if("org.h2.Driver".equals(className)){

            }
            return Collections.EMPTY_MAP;
        }

        static Map<String, String> query(String sql, Object ... table){
            Map<String, String> map = new LinkedHashMap<>();
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                for (int i = 0; i < table.length; i++) {
                    ps.setObject(i+1, table[i]);
                }
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    map.put(rs.getString(1), rs.getString(2));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return map;
        }

        static void allTable() throws Exception{
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
            while(tables.next()){
                try{
                    for (int i = 1; ; i++) {
                        System.out.print(i + " " + tables.getObject(i)+" ; ");
                    }
                }catch (Exception e){
                    System.err.println(e.getMessage());
                }
            }
        }

        @Data
        static class Table{
            private String tableName;
            private String tableComments;
            private List<Col> cols;
        }

        @Data
        static class Col{
            private String colName;
            private String colType;
            private String colComments;
        }
    }

    // =================================================================================================================

    public static LinkedOps ready(){
        return new LinkedOps();
    }

    public static class LinkedOps{

        public static final int INSERT = 1;
        public static final int DELETE = 2;
        public static final int UPDATE = 3;
        public static final int SELECT = 4;

        private List<ColumnCall> columnCalls = new LinkedList<>();
        private List<ColumnCall> whereCalls = new LinkedList<>();
        private List<ColumnCall> currCalls = whereCalls;
        private Object columnObj;
        private List columnObjs;
        private Object whereObj;
        private int opsType;
        private boolean where;

        public LinkedOps ignoreNull(){
            return addCall(IGNORE_NULL);
        }
        public LinkedOps selAs(){
            return addCall(SEL_AS);
        }
        public LinkedOps byCol(String fieldNames){
            return addCall(BYCOL(fieldNames));
        }
        public LinkedOps custom(ColumnCall call){
            return addCall(call);
        }
        public LinkedOps insert(){
            return insert(null);
        }
        public LinkedOps insert(Object obj){
            return opsType(obj, columnCalls, INSERT);
        }
        public LinkedOps delete(){
            return delete(null);
        }
        public LinkedOps delete(Object obj){
            return opsType(obj, columnCalls, DELETE);
        }
        public LinkedOps update(){
            return update(null);
        }
        public LinkedOps update(Object obj){
            return opsType(obj, columnCalls, UPDATE);
        }
        public LinkedOps select(){
            return select(null);
        }
        public LinkedOps select(Object obj){
            return opsType(obj, columnCalls, SELECT);
        }
        public LinkedOps where(){
            return where(null);
        }
        public LinkedOps where(Object obj){
            this.whereObj = obj;
            this.currCalls = whereCalls;
            this.where = true;
            return this;
        }
        public BatchSqlObj go(List list){
            this.columnObjs = list;
            List<Column> columns = new LinkedList<>();
            columnCalls.add(column -> {
                if(column.ignore) return;
                columns.add(column);
            });
            whereCalls.add(column -> {
                if(column.ignore) return;
                columns.add(column);
            });
            SqlObj sqlObj = go();
            List<Object[]> batchArgs = new LinkedList<>();
            for (Object obj : columnObjs) {
                List args = new LinkedList();
                for (Column column : columns) {
                    if(empty(column.extSql)){
                        args.add(fieldValue(column.getFiled(), obj));
                    }else{
                        args.addAll(arrsToList(column.getExtSqlArgs()));
                    }
                }
                batchArgs.add(args.toArray());
            }
            return batchSqlObj(sqlObj.sql, batchArgs);
        }
        public SqlObj go(){
            toBefore();
            SqlObj whereSql = sqlObj("", Arrays.asList());
            if(opsType == INSERT){
                return insertSql(columnObj, columnCalls.toArray(new ColumnCall[]{}));
            }else if(opsType == DELETE){
                whereSql.sql = "delete from " + tableName(columnObj) + " " + (empty(whereSql.sql) ? "" : "where " + whereSql.sql);
                return whereSql;
            }else if(opsType == UPDATE){
                SqlObj updateSql = updateSql(columnObj, columnCalls.toArray(new ColumnCall[]{}));
                if(where) whereSql = whereSql(whereObj, whereCalls.toArray(new ColumnCall[]{}));
                updateSql.sql  = updateSql.sql + (empty(whereSql.sql) ? "" : "where " + whereSql.sql);
                updateSql.args = arrsToList(updateSql.args, whereSql.args).toArray();
                return updateSql;
            }else if(opsType == SELECT){
                SqlObj selectSql = selectSql(columnObj, columnCalls.toArray(new ColumnCall[]{}));
                if(where) whereSql = whereSql(whereObj, whereCalls.toArray(new ColumnCall[]{}));
                selectSql.sql  = selectSql.sql + (empty(whereSql.sql) ? "" : "where " + whereSql.sql);
                selectSql.args = arrsToList(selectSql.args, whereSql.args).toArray();
                return selectSql;
            }
            throw new RuntimeException("unknown ops type");
        }
        public void toBefore(){
            Object base = columnObj;
            if(base == null) base = whereObj;
            if(columnObjs!=null){
                for (Object obj : columnObjs) {
                    if(obj != null){
                        base = obj;
                        break;
                    }
                }
            }
            isTree(base != null, "must be a obj");
            if(columnObj == null) columnObj = base;
            if(whereObj == null) whereObj = base;
        }
        private LinkedOps opsType(Object obj, List<ColumnCall> columnCalls, int opsType){
            this.columnObj = obj;
            this.currCalls = columnCalls;
            this.opsType = opsType;
            return this;
        }
        private LinkedOps addCall(ColumnCall call){
            currCalls.add(call);
            return this;
        }
    }

}
