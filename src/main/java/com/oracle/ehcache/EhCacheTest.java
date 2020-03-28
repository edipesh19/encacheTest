package com.oracle.ehcache;

import java.io.File;
import java.time.Duration;
import com.oracle.dicom.agent.mediator.dto.AgentMessageResultDTO;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;

public class EhCacheTest {

    private static PersistentCacheManager cacheManager;
    private static Cache<String, MessageResultWrapper> myCache;

    public static void main(String[] args) throws InterruptedException {
        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(new EhcacheEventListener(), EventType.CREATED, EventType.REMOVED, EventType.EXPIRED,
                EventType.EVICTED)
            .unordered().asynchronous();

        CacheConfiguration<String, MessageResultWrapper> cacheConfig =
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, MessageResultWrapper.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1, EntryUnit.ENTRIES)
                    .disk(5, MemoryUnit.MB, true))
                .withService(cacheEventListenerConfiguration)
                .withValueSerializer(AgentMessageEhCacheSerializer.class)
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(10)))
                .withEvictionAdvisor(new EhcacheEvictionAdvisor())
                .build();
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(new CacheManagerPersistenceConfiguration(new File("./build")))
            .withCache("myCache", cacheConfig)
            .build(true);

        myCache = cacheManager.getCache("myCache", String.class, MessageResultWrapper.class);
        //myCache.getRuntimeConfiguration().registerCacheEventListener(new EhcacheEventListener(), EventOrdering.UNORDERED, EventFiring.ASYNCHRONOUS,
        //  EnumSet.of(EventType.CREATED, EventType.EXPIRED));
        EhCacheTest ehCacheTest = new EhCacheTest();
        //ehCacheTest.concurrentTest();
        //ehCacheTest.getNRecords(100);
        ehCacheTest.insertNRecords(5);
        Thread.sleep(1000);
        //ehCacheTest.getNRecords(1);
        //ehCacheTest.getAndRemoveNRecords(2);
        //Thread.sleep(20000);
        ehCacheTest.getNRecords(5);
        Thread.sleep(60000);
        //Thread.sleep(1000);
        //cacheManager.close();

        /*Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown Hook is running !");
                cacheManager.close();
            }
        });*/

        System.out.println("Application terminated");
    }

    public Thread getProducer() {
        return new Thread() {
            public void run() {
                putMessageInCache();
            }
        };
    }

    public Thread getConsumer() {
        return new Thread() {
            public void run() {
                getMessageFromCache();
            }
        };
    }

    public Thread getRemover() {
        return new Thread() {
            public void run() {
                removeFromCache();
            }
        };
    }

    private void removeFromCache() {
        Integer key = 1;
        while (true) {
            myCache.remove(key.toString());
            String str = "REMOVER : key = " + key;
            System.out.println(str);
            key++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getMessageFromCache() {
        Integer key = 1;
        while (true) {
            MessageResultWrapper val = myCache.get(key.toString());
            String str;
            if (val != null) {
                str = "CONSUMER : key = " + key + " value = " + val.getMessage().getMsgId();
            } else {
                str = "CONSUMER : key = " + key + " value = null";
            }
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
        while (true) {
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
        Thread r = this.getRemover();
        p.start();
        Thread.sleep(100);
        c.start();
        Thread.sleep(1000);
        r.start();

        p.join();
        //c.join();
        //r.join();
    }

    public void insertNRecords(int n) {
        for (int i = 0; i < n; i++) {
            MessageResultWrapper val = new MessageResultWrapper(new AgentMessageResultDTO());
            val.getMessage().setMsgId("QWERTY" + i);
            if (i >=3) {
                val.markMessageForDelete();
            }
            //System.out.println("Inserting id: " + val.getMessage().getMsgId());
            myCache.put("" + i, val);
        }
    }

    public void getNRecords(int n) {
        for (int i = 0; i < n; i++) {
            MessageResultWrapper val = myCache.get("" + i);
            System.out.println("Record: Key: " + i + " Val: " + val);
        }
    }

    public void getAndRemoveNRecords(int n) {
        for (int i = 0; i < n; i++) {
            MessageResultWrapper val = myCache.get("" + i);
            System.out.println("Remove: Key: " + i + " Val: " + val);
            myCache.remove("" + i);
        }
    }

}
