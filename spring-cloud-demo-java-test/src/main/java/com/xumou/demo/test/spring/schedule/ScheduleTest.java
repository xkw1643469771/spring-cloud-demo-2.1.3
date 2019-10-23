package com.xumou.demo.test.spring.schedule;

import org.junit.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduleTest {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        testScheduledThreadPoolExecutor();
    }

    // 测试Java自带定时，下面的是对这个的封装
    public static void testScheduledThreadPoolExecutor(){
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(()-> System.out.println(123), 5, TimeUnit.SECONDS);
    }

    // 测试Java自带定时
    public static void testJavaScheduler() throws Exception{
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.schedule(()->System.out.println("test"), 5, TimeUnit.SECONDS);
    }

    // 测试Spring定时
    public static void testTaskScheduler() throws Exception{
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler();
        taskScheduler.schedule(() -> System.out.println("时间"), format.parse("2019-10-23 20:47:00"));
        taskScheduler.schedule(() -> System.out.println("表达式1"), new CronTrigger("0/5 * * * * ?"));
        taskScheduler.scheduleAtFixedRate(() -> System.out.println("AtFixedRate"), 5000);
        taskScheduler.scheduleWithFixedDelay(() -> System.out.println("WithFixedDelay"), 5000);
    }

    // 解析Cron表达式
    @Test
    public void testCronTrigger(){
        CronTrigger trigger = new CronTrigger("0/5 * * * * ?");
    }

}
