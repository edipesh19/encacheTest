package com.oracle.ehcache;

import com.oracle.dicom.agent.mediator.dto.AgentMessageResultDTO;
import org.ehcache.Cache;
import org.ehcache.CachePersistenceException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;

public class EhCacheTest {
    private static Logger logger = LoggerFactory.getLogger(EhCacheTest.class.getName());

    private static PersistentCacheManager cacheManager;
    private static Cache<String, MessageResultWrapper> myCache;
    private EhcacheEventListener listner;

    public EhCacheTest(EhcacheEventListener listner) {
        this.listner = listner;
    }

    public static void main(String[] args) throws InterruptedException {

        EhCacheTest ehCacheTest = new EhCacheTest(new EhcacheEventListener());
        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(ehCacheTest.listner, EventType.CREATED, EventType.REMOVED, EventType.EVICTED)
            .unordered().asynchronous();

        CacheConfiguration<String, MessageResultWrapper> cacheConfig =
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, MessageResultWrapper.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1, EntryUnit.ENTRIES)
                    .disk(5, MemoryUnit.MB, true))
                .withService(cacheEventListenerConfiguration)
                .withValueSerializer(AgentMessageEhCacheSerializer.class)
                //.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2)))
                .build();
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(new CacheManagerPersistenceConfiguration(new File("./build")))
            .withCache("myCache", cacheConfig)
            .build(true);

        myCache = cacheManager.getCache("myCache", String.class, MessageResultWrapper.class);
        //myCache.getRuntimeConfiguration().registerCacheEventListener(new EhcacheEventListener(), EventOrdering.UNORDERED, EventFiring.ASYNCHRONOUS,
        //  EnumSet.of(EventType.CREATED, EventType.EXPIRED));

        ehCacheTest.concurrentTest();
        //ehCacheTest.getNRecords(100);
        //ehCacheTest.insertNRecords(100);
        //Thread.sleep(1000);
        //ehCacheTest.getNRecords(100);
        //Thread.sleep(1000);
        //ehCacheTest.getAndRemoveNRecords(100);
        //cacheManager.close();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " +ehCacheTest.listner.getA());
                logger.info("METRIC : {} value {}", Thread.currentThread().getName(), ehCacheTest.listner.getA());
                System.out.println("Shutdown Hook is running !");
                logger.info("Shutdown Hook is running !");
                cacheManager.close();
                try {
                    cacheManager.destroy();
                } catch (CachePersistenceException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread.sleep(1000*60*1); // Sleep 1 minute
        System.out.println("METRIC : "  + Thread.currentThread().getName() + " " +ehCacheTest.listner.getA());
        logger.info("METRIC : {} value {}", Thread.currentThread().getName(), ehCacheTest.listner.getA());
        System.out.println("Application terminated");
        logger.info("Application terminated");
    }

    public Thread getProducer(int start) {
        ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();
        return new Thread("Producer " + start) {
            public void run() {
                threadLocal.set(start);
                putMessageInCache(start);
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

    public Thread getRemover(int start) {
        ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();
        return new Thread("Remover " + start ) {
            public void run() {
                removeFromCache(start);
            }
        };
    }

    private void removeFromCache(int start) {
        int end = (start+1)*1000;
        Integer key = end -999;
        while (true && key <= end) {
            myCache.remove(key.toString());
            String str = Thread.currentThread().getName() +" : REMOVER : key = " + key;
            logger.info(str);
            key++;
        }
        System.out.println("METRIC : "  +  Thread.currentThread().getName() + " " + listner.getA());
        logger.info("METRIC : {} value {}", Thread.currentThread().getName(), listner.getA());
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

    public void putMessageInCache(int start) {
        int end = (start+1)*1000;
        Integer key = end-999;
        while (true && key <= end) {
            MessageResultWrapper val = new MessageResultWrapper(new AgentMessageResultDTO());
            val.getMessage().setMsgId("QWERTY " + key);
            String str = Thread.currentThread().getName() + " : PRODUCER : key = " + key + " value = " + val.getMessage().getMsgId();
            logger.info(str);
            myCache.put(key.toString(), val);
            key++;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("METRIC : "  +  Thread.currentThread().getName() + " " +listner.getA());
        logger.info("METRIC : {} value {}", Thread.currentThread().getName(), listner.getA());
    }

    public void concurrentTest() throws InterruptedException {

        int threadSize = 100;
        Thread[] pArr = new Thread[threadSize];
        for(int i = 0; i < threadSize; i++) {
            pArr[i] = this.getProducer(i);
            pArr[i].start();
        }
        for(int i = 0; i < threadSize; i++) { {
            pArr[i].join();
        }}


        Thread.sleep(100);

        Thread[] cArr = new Thread[threadSize];
        for(int i = 0; i < threadSize; i++) {
            cArr[i] = this.getRemover(i);
            cArr[i].start();
        }
        for(int i = 0; i < threadSize; i++) { {
            cArr[i].join();
        }}
    }

    public void insertNRecords(int n) {
        for (int i = 0; i < n; i++) {
            MessageResultWrapper val = new MessageResultWrapper(new AgentMessageResultDTO());
            val.getMessage().setMsgId("QWERTY" + i);
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
