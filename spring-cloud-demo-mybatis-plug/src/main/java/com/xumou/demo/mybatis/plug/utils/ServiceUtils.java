package com.xumou.demo.mybatis.plug.utils;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class ServiceUtils {

    private static ApplicationContext context;
    private static CommonMapper commonMapper;

    @Autowired
    private ServiceUtils(ApplicationContext context, CommonMapper commonMapper){
        ServiceUtils.context = context;
        ServiceUtils.commonMapper = commonMapper;
    }

    public static List<Map<String, Object>> executeSql(String sql){
        return commonMapper.executeSql(sql);
    }

    public static <T> T lastInsertId(){
        List<Map<String, Object>> maps = commonMapper.executeSql("select last_insert_id() id");
        return (T)maps.get(0).get("id");
    }

    @Mapper
    public interface CommonMapper{

        @Select("${sql}")
        List<Map<String, Object>> executeSql(@Param("sql") String sql);

    }

}
