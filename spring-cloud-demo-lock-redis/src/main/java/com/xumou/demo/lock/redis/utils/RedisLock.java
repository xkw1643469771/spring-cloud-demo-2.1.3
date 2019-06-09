package com.xumou.demo.lock.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于redis的可重入锁
 */
@Component
public class RedisLock {

    private static StringRedisTemplate stringRedisTemplate;

    private static final int TIMEOUT = 20;
    private static final int RETRY_TIME = 1000;

    @Autowired
    private RedisLock(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public static void lock(String key){
        lock(key, RETRY_TIME);
    }

    public static void lock(String key, long retryTime){
        Boolean result = tryLock(key);
        while(!result){
            sleep(retryTime);
            result = tryLock(key);
        }
    }

    public static boolean tryLock(String key){
        String lockKey  = getLockKey();
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, lockKey, TIMEOUT, TimeUnit.SECONDS);
        if(result){
            countAdd();
            Var.treeMapSynchronized(Var.ADD, Var.generatorKey(), new LockObj(key, lockKey));
        }else{
            String val = stringRedisTemplate.opsForValue().get(key);
            if(lockKey.equals(val)){
                countAdd();
                result = true;
            }
        }
        return result;
    }

    public static void unlock(String key){
        countSub();
        if(isUnlock()){
            String lockKey  = getLockKey();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) return 'yes' else return 'no' end";
            stringRedisTemplate.execute(RedisScript.of(script), Arrays.asList(key), lockKey);
        }
    }

    private static boolean lockTimeUpdate(LockObj lockObj){
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] and redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2], 'XX') then return 'yes' else return 'no' end";
        Object result = stringRedisTemplate.execute(RedisScript.of(script, String.class), Arrays.asList(lockObj.key), lockObj.value, String.valueOf(TIMEOUT));
        return "yes".equals(result);
    }

    private static String getLockKey() {
        if(Var.lockKey.get() == null){
            Var.lockKey.set(UUID.randomUUID().toString());
        }
        return Var.lockKey.get();
    }

    public static void sleep(long timer){
        if(timer <= 0){
            return;
        }
        try {
            Thread.sleep(timer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void countAdd(){
        Integer integer = Var.count.get();
        if(integer == null){
            integer = 0;
        }
        Var.count.set(integer + 1);
    }

    private static void countSub(){
        Integer integer = Var.count.get();
        if(integer != null) {
            Var.count.set(integer - 1);
        }
    }

    private static boolean isUnlock(){
        Integer integer = Var.count.get();
        if(integer != null){
            return integer <= 0;
        }else{
            return true;
        }
    }

    private static class Var{

        private static ThreadLocal<String> lockKey = new ThreadLocal<>();
        private static ThreadLocal<Integer> count = new ThreadLocal<>();
        private static TreeMap<Long, LockObj> treeMap = new TreeMap<>();
        private static boolean flag = false;

        static final int ADD = 1;
        static final int DEL = 2;
        static final int GET = 3;
        static final int GET_FIRST_ENTER = 4;

        static final long INTERVAL = TIMEOUT * 500;

        static long generatorKey(){
            return  System.currentTimeMillis() + INTERVAL;
        }

        static{
            new Thread(() -> {
                while(true){
                    try{
                        Map.Entry<Long, LockObj> entry = treeMapSynchronized(GET_FIRST_ENTER, null, null);
                        treeMapWait(entry.getKey() - System.currentTimeMillis());
                        if(flag){
                            flag = false;
                        }else{
                            boolean result = lockTimeUpdate(entry.getValue());
                            treeMapSynchronized(DEL, entry.getKey(), null);
                            if(result){
                                treeMapSynchronized(ADD, generatorKey(), entry.getValue());
                            }
                        }
                    }catch(Exception e){
                        treeMapWait(INTERVAL);
                    }
                }
            }).start();
        }

        static void treeMapWait(long timer){
            if(timer <= 0){
                return;
            }
            synchronized(treeMap){
                try {
                    treeMap.wait(timer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        static void treeMapNotifyAll(){
            synchronized(treeMap){
                treeMap.notifyAll();
                flag = true;
            }
        }


        static synchronized <T> T treeMapSynchronized(int ops, Long key, LockObj lockObj){
            switch (ops){
                case ADD :
                    treeMap.put(key, lockObj);
                    treeMapNotifyAll();
                    return null;
                case DEL :
                    treeMap.remove(key);
                    return null;
                case GET :
                    return (T)treeMap.get(key);
                case GET_FIRST_ENTER :
                    return (T)treeMap.firstEntry();
                default:
                    throw new RuntimeException("无效操作类型");
            }
        }

    }

    private static class LockObj {
        private final String key;
        private final String value;
        public LockObj(String key, String value){
            this.key = key;
            this.value = value;
        }
    }

}
