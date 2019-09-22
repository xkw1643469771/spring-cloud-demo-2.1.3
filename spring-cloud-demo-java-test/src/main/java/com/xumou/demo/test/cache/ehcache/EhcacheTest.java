package com.xumou.demo.test.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class EhcacheTest {

    private CacheManager manager;

    @Before
    public void before(){
//        String path = this.getClass().getClassLoader().getResource("").getFile();
        String path = "D:/temp/chche/ehcache/test";
        Configuration configuration = ConfigurationFactory.parseConfiguration();
        configuration.getDiskStoreConfiguration().setPath(path);
        manager = CacheManager.create(configuration);
    }

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

    @Test
    public void test2(){
        CacheConfiguration conf = new CacheConfiguration();
        conf.setName("test2");
        conf.setMaxEntriesLocalHeap(1);     // 内存最大缓存数量
        conf.setMaxEntriesLocalDisk(0);     // 磁盘最大缓存数量
        conf.setOverflowToDisk(true);       // 内存达到最大后缓存到磁盘
        Cache cache = new Cache(conf);
        manager.addCache(cache);
        for (int i = 0; i < 10000*100; i++) {
            cache.put(new Element(i, i));
        }
    }

    @Test
    public void test3() throws IOException {
        File test2 = manager.getDiskStorePathManager().getFile("test2.data");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(test2)));
        for (String i = br.readLine(); i != null; i = br.readLine()) {
            System.out.println(br.read());
        }
    }
}
