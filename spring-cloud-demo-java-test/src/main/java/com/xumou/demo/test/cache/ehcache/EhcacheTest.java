package com.xumou.demo.test.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Test;

public class EhcacheTest {

    private static CacheManager manager = new CacheManager();

    @Test
    public void test1(){
        CacheConfiguration conf = new CacheConfiguration();
        conf.setName("test1");
        conf.setMaxEntriesLocalHeap(1);     // 内存最大缓存数量
        conf.setMaxEntriesLocalDisk(1);     // 磁盘最大缓存数量
        conf.setOverflowToDisk(true);       // 内存达到最大后缓存到磁盘
        Cache cache = new Cache(conf);
        manager.addCache(cache);
        cache.put(new Element("a", "123"));
        cache.put(new Element("b", "345"));
        cache.put(new Element("c", "567"));
        System.out.println(cache.get("a"));
        System.out.println(cache.get("b"));
        System.out.println(cache.get("c"));
    }

}
