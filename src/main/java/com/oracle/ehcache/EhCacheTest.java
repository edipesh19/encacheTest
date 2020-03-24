package com.oracle.ehcache;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

import java.io.File;

public class EhCacheTest {

    private static int counter = 1;
    private static PersistentCacheManager cacheManager;
    private static Cache<Integer, String> myCache;

    public Thread getProducer(){
        return new Thread() {
            public void run() {
                putMessageInCache();
            }
        };
    }

    public Thread getConsumer(){
        return new Thread() {
            public void run() {
                getMessageFromCache();
            }
        };
    }

    public void getMessageFromCache() {
        int key = 1;
        while(true) {
            String val = myCache.get(key);
            String str = String.format("CONSUMER : key = {}, value = {}", key, val);
            System.out.println(str);
            key++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void putMessageInCache() {
        int key = 1;
        while(true) {
            String val = "Message entry: " + key;
            String str = String.format("PRODUCER: key = {}, value = {}",key,val);
            System.out.println(str);
            myCache.put(key, val);
            key++;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(new File("./build", "myData")))
            .withCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, String.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(2, EntryUnit.ENTRIES)
                        .disk(5, MemoryUnit.MB, true)
                )).build(true);
        myCache = cacheManager.getCache("myCache", Integer.class, String.class);
        EhCacheTest ehCacheTest = new EhCacheTest();


        Thread p = ehCacheTest.getProducer();
        Thread c = ehCacheTest.getConsumer();
        p.start();
        c.start();
        Thread.sleep(1000);
        ehCacheTest.getConsumer().start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown Hook is running !");
                cacheManager.close();
            }
        });

        p.join();
        c.join();
        System.out.println("Application terminated");
    }

}
