package com.oracle.ehcache;

import org.ehcache.impl.events.CacheEventAdapter;

public class EhcacheEventListener extends CacheEventAdapter<String, MessageResultWrapper> {

    public EhcacheEventListener() {
    }

    protected void onCreation(String key, MessageResultWrapper newValue) {
        System.out.println("Event : Created Key : " + key);
    }

    protected void onRemoval(String key, MessageResultWrapper removedValue) {
        System.out.println("Event : Removed Key : " + key);
    }

    protected void onExpiry(String key, MessageResultWrapper expiredValue) {
        System.out.println("Event : Expired Key : " + key);
    }

    protected void onEviction(String key, MessageResultWrapper expiredValue) {
        System.out.println("Event : Evicted Key : " + key);
    }
}