package com.xumou.demo.mybatis.plug.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xumou.demo.mybatis.plug.dao.TestDao;
import com.xumou.demo.mybatis.plug.po.Test;
import com.xumou.demo.mybatis.plug.utils.ServiceUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService extends ServiceImpl<TestDao, Test> {

    @Transactional
    public Object test() {
        String sql = "insert into tbl_test (id, name) values (null, '1111111111111111')";
        ServiceUtils.executeSql(sql);
        return ServiceUtils.lastInsertId();
    }

    public Object test4() {
        QueryWrapper<Test> warpper = new QueryWrapper<>();
        warpper.and(val -> val.eq("id", "123").or().eq("id", "456"));
        warpper.and(val -> val.eq("name", "123").or().eq("name","456"));
        return this.list(warpper);
    }
}
