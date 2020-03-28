package com.oracle.ehcache;

import org.ehcache.config.EvictionAdvisor;

public class EhcacheEvictionAdvisor implements EvictionAdvisor<String, MessageResultWrapper> {

    public static final String PROP_KEY_MSG_MARKED_FOR_DELETE = "dicom.agent.messaging.message.markedForDelete";

    @Override
    public boolean adviseAgainstEviction(String key, MessageResultWrapper value) {
        boolean res = value.getMessageProperties().get(PROP_KEY_MSG_MARKED_FOR_DELETE).equals(Boolean.FALSE.toString());
        System.out.println("EhcacheEvictionAdvisor: Returning" + res + " Key: " + key);
        return res;
    }
}
