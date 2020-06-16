package ehcache;

import org.ehcache.impl.events.CacheEventAdapter;

import java.util.concurrent.atomic.AtomicLong;

public class EhcacheEventListener extends CacheEventAdapter<String, MessageResultWrapper> {

    private AtomicLong a;
    public EhcacheEventListener() {
        a = new AtomicLong(0L);
    }

    protected void onCreation(String key, MessageResultWrapper newValue) {
        //System.out.println("Event : Created Key : " + key);
        a.getAndIncrement();
    }

    protected void onRemoval(String key, MessageResultWrapper removedValue) {
        //System.out.println("Event : Removed Key : " + key);
        a.getAndDecrement();
    }

    protected void onExpiry(String key, MessageResultWrapper expiredValue) {
        System.out.println("Event : Expired Key : " + key);
    }

    protected void onEviction(String key, MessageResultWrapper expiredValue) {
        System.out.println("Event : Evicted Key : " + key);
        a.getAndDecrement();
    }

    public AtomicLong getA() {
        return a;
    }
}