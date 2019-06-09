package com.xumou.demo.lock.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public static void lock(String key, String lockKey){
        lock(key, lockKey, RETRY_TIME);
    }

    public static void lock(String key, long retryTime){
        lock(key, getLockKey(), retryTime);
    }

    public static boolean tryLock(String key){
        return tryLock(key, getLockKey());
    }

    public static void lock(String key, String lockKey, long retryTime){
        Boolean result = tryLock(key, lockKey);
        while(!result){
            sleep(retryTime);
            result = tryLock(key, lockKey);
        }
    }

    public static boolean tryLock(String key, String lockKey){
        String script = "if redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then return else return redis.call('get', KEYS[1]) end";
        Object execute = stringRedisTemplate.execute(RedisScript.of(script, String.class), Arrays.asList(key), lockKey, String.valueOf(TIMEOUT));
        if(execute == null){
            countAdd(key, lockKey);
            Var.treeMapSynchronized(Var.ADD, Var.generatorKey(), new LockObj(key, lockKey));
            return true;
        }else{
            if(execute.equals(lockKey)){
                countAdd(key, lockKey);
                return true;
            }else{
                return false;
            }
        }
    }

    public static void unlock(String key){
        unlock(key, getLockKey());
    }

    public static void unlock(String key, String lockKey){
        if(countSub(key, lockKey)){
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) return 'yes' else return 'no' end";
            stringRedisTemplate.execute(RedisScript.of(script), Arrays.asList(key), lockKey);
            Var.treeMapSynchronized(Var.DEL_UNLOCK_KEY,null, new LockObj(key, lockKey));
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

    private static void countAdd(String key, String lockKey){
        String countKey = countKey(key, lockKey);
        Integer integer = Var.countMap.get(countKey);
        if(integer == null){
            integer = 0;
        }
        Var.countMap.put(countKey, integer + 1);
    }

    private static boolean countSub(String key, String lockKey){
        String countKey = countKey(key, lockKey);
        Integer integer = Var.countMap.get(countKey);
        if(integer != null) {
            integer --;
            boolean result = integer <= 0;
            if(result){
                Var.countMap.remove(countKey(key, lockKey));
            }else{
                Var.countMap.put(countKey, integer);
            }
            return result;
        }else{
            return true;
        }
    }

    private static String countKey(String key, String lockKey){
        return key + "|" + lockKey;
    }

    private static class Var{

        private static ThreadLocal<String> lockKey = new ThreadLocal<>();
        private static TreeMap<Long, LockObj> treeMap = new TreeMap<>();
        private static Map<String, Integer> countMap = new ConcurrentHashMap<>();
        private static boolean flag = false;

        private static final Object lock = new Object();

        static final int ADD = 1;
        static final int DEL = 2;
        static final int GET = 3;
        static final int GET_FIRST_ENTER = 4;
        static final int DEL_UNLOCK_KEY = 5;

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
                            }else{
                                countMap.remove(countKey(entry.getValue().key, entry.getValue().value));
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
            synchronized(lock){
                try {
                    lock.wait(timer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        static void treeMapNotifyAll(){
            synchronized(lock){
                lock.notifyAll();
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
                case DEL_UNLOCK_KEY :
                    Iterator<Map.Entry<Long, LockObj>> iterator = treeMap.entrySet().iterator();
                    while(iterator.hasNext()){
                        Map.Entry<Long, LockObj> next = iterator.next();
                        if(next.getValue().key.equals(lockObj.key) && next.getValue().value.equals(lockObj.value)){
                            iterator.remove();
                        }
                    }
                    return null;
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
