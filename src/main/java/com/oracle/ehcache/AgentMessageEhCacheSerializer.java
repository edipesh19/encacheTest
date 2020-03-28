package com.oracle.ehcache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

public class AgentMessageEhCacheSerializer implements Serializer<MessageResultWrapper> {

    private String encoding = "UTF-8";
    private static ObjectMapper c_objMapper;

    public AgentMessageEhCacheSerializer() {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AgentMessageEhCacheSerializer(ClassLoader loader) {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ByteBuffer serialize(MessageResultWrapper object) throws SerializerException {
        try {
            if (object == null) {
                return null;
            } else {
                ByteBuffer b =  ByteBuffer.wrap(c_objMapper.writeValueAsString(object).getBytes(this.encoding));
                return b;
            }
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            String errMsg = "Error when serializing MessageResultWrapper to byte[]";
            System.out.println(errMsg);
            return null;
        }
    }

    @Override
    public MessageResultWrapper read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        try {
            if (binary == null) {
                return null;
            } else {
                return c_objMapper.readValue(new String(binary.array(), this.encoding), MessageResultWrapper.class);
            }
        } catch (IOException e) {
            String errMsg = "Error when reading MessageResultWrapper to byte[]";
            e.printStackTrace();
            System.out.println(errMsg);
            return null;
        }
    }

    @Override
    public boolean equals(MessageResultWrapper object, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        return object.equals(read(binary));
    }
}
