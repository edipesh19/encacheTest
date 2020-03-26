package com.oracle.ehcache;

import com.oracle.dicom.agent.mediator.dto.AgentMessageResultDTO;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;

import java.io.File;
import java.sql.SQLOutput;

public class EhCacheTest {

    private static int counter = 1;
    private static PersistentCacheManager cacheManager;
    private static Cache<String, MessageResultWrapper> myCache;

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
        Integer key = 1;
        while(true) {
            MessageResultWrapper val = myCache.get(key.toString());
            String str = "CONSUMER : key = " + key + " value = " + val;
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
        Integer key = 1;
        while(true) {
            MessageResultWrapper val = new MessageResultWrapper(new AgentMessageResultDTO());
            val.getMessage().setMsgId("QWERTY " + key);
            String str = "PRODUCER : key = " + key + " value = " + val.getMessage().getMsgId();
            System.out.println(str);
            myCache.put(key.toString(), val);
            key++;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void concurrentTest() throws InterruptedException {
        Thread p = this.getProducer();
        Thread c = this.getConsumer();
        p.start();
        Thread.sleep(100);
        c.start();

        p.join();
        //c.join();
    }

    public void insertNRecords(int n) {
        for (int i=0;i<n;i++) {
            MessageResultWrapper val = new MessageResultWrapper(new AgentMessageResultDTO(), false);
            val.getMessage().setMsgId("QWERTY" + i);
            System.out.println("Inserting id: " + val.getMessage().getMsgId());
            myCache.put(""+i, val);
        }
    }

    public void getNRecords(int n) {
        for (int i=0;i<n;i++) {
            MessageResultWrapper val = myCache.get(""+i);
            System.out.println("Record: Key: " + i + " Val: " + val);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        //cacheManager = (PersistentCacheManager) CacheManagerBuilder.newCacheManagerBuilder().build(true);
        CacheConfiguration<String, MessageResultWrapper> cacheConfig =
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, MessageResultWrapper.class,
                  ResourcePoolsBuilder.newResourcePoolsBuilder()
                      //.heap(2, EntryUnit.ENTRIES)
                      .disk(100, MemoryUnit.MB, true))
                  .withValueSerializer(KryoSerializer.class)
                  .build();
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(new CacheManagerPersistenceConfiguration(new File("./build")))
            .withCache("myCache", cacheConfig)
            .build(true);

        /*cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(new File("./build", "myData")))
            .withCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, MessageResultWrapper.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(10, EntryUnit.ENTRIES)
                        .disk(100, MemoryUnit.MB, true)
                )).build(true);*/
        myCache = cacheManager.getCache("myCache", String.class, MessageResultWrapper.class);
        EhCacheTest ehCacheTest = new EhCacheTest();
        //ehCacheTest.concurrentTest();

        ehCacheTest.insertNRecords(1);
        Thread.sleep(1000);
        ehCacheTest.getNRecords(1);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown Hook is running !");
                cacheManager.close();
            }
        });

        System.out.println("Application terminated");
    }

}
