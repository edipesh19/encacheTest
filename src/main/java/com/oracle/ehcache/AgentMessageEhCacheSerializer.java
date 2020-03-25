package com.oracle.ehcache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oracle.dicom.agent.mediator.util.MediatorUtil;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

/**
 * Serialize instances of AgentMessageDTO to byte[].
 * <p>
 * An instance of AgentMessageDTO is serialized to a String object in JSON format first.
 * A byte array is obtained from this String object.
 */
public class AgentMessageEhCacheSerializer implements Serializer<MessageResultWrapper> {
    public AgentMessageEhCacheSerializer() {
    }

    private String encoding = "UTF8";

    public AgentMessageEhCacheSerializer(ClassLoader loader) {
    }

    @Override
    public ByteBuffer serialize(MessageResultWrapper object) throws SerializerException {
        try {
            if (object == null) {
                return null;
            } else {
                ByteBuffer b =  ByteBuffer.wrap(MediatorUtil.toJson(object).getBytes(encoding));
                return b;
            }
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            String errMsg = "Error when serializing AgentMessageDTO to byte[]";
            // Do not throw exception here to avoid poison pills issue please check kafka FAQ for details
            // https://docs.confluent.io/current/streams/faq.html#streams-faq-failure-handling-deserialization-errors-quarantine
            return null;
        }
    }

    @Override
    public MessageResultWrapper read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        try {
            if (binary == null) {
                return null;
            } else {
                return MediatorUtil.fromJson(new String(binary.array(), this.encoding), MessageResultWrapper.class);
            }
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            String errMsg = "Error when serializing AgentMessageDTO to byte[]";
            // Do not throw exception here to avoid poison pills issue please check kafka FAQ for details
            // https://docs.confluent.io/current/streams/faq.html#streams-faq-failure-handling-deserialization-errors-quarantine
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(MessageResultWrapper object, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        return object.equals(read(binary));
    }
}
