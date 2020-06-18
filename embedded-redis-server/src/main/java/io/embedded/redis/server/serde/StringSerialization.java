package io.embedded.redis.server.serde;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class StringSerialization implements RedisSerializer<String> {
    private final String encoding = "UTF8";
    @Override
    public byte[] serialize(String data) throws SerializationException {
        try {
            if (data == null) {
                return null;
            } else {
                return data.getBytes(encoding);
            }
        } catch (Exception e) {
            String errMsg = "Error when serializing String to byte[]";
            //logger.error(errMsg, e);
            return null; // Todo : throw exception instead returning null
        }
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializationException {
        try {
            if (bytes == null) {
                return null;
            } else {
                return new String(bytes, encoding);
            }
        } catch (Exception e) {
            // String errMsg = "Error when de-serializing byte[] to String";
            //logger.error(errMsg, e);
            // Do not throw exception here to avoid poison pills issue please check kafka FAQ for details
            // https://docs.confluent.io/current/streams/faq.html#streams-faq-failure-handling-deserialization-errors-quarantine
            return null; // Todo : throw exception instead returning null
        }
    }
}
