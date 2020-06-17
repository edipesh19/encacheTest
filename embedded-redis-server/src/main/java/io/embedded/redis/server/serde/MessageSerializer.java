package io.embedded.redis.server.serde;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.embedded.redis.server.dto.MessageDTO;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MessageSerializer implements RedisSerializer<MessageDTO> {

    private final String encoding = "UTF8";

    private static ObjectMapper c_objMapper;

    public MessageSerializer() {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public byte[] serialize(MessageDTO messageDTO) throws SerializationException {
        try {
            if (messageDTO == null) {
                //logger.warn("Null AgentMessageDTO reference passed for serialization, null record will be stored in topic:" + topic);
                return null;
            } else {
                return toJson(messageDTO).getBytes(encoding);
            }
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            //String errMsg = "Error when serializing AgentMessageDTO to byte[]";
            //logger.error(errMsg, e);
            return null; // Todo : throw exception instead returning null
        }
    }

    @Override
    public MessageDTO deserialize(byte[] bytes) throws SerializationException {
        try {
            if (bytes == null) {
                //logger.warn("Null byte[] passed for deserialization, null AgentMessageResultDTO pointer will be returned, topic:" + topic);
                return null;
            } else {
                return fromJson(new String(bytes, this.encoding), MessageDTO.class);
            }
        } catch (Exception e) {
            //String errMsg = "Error when deserializing byte[] to AgentMessageResultDTO";
            //logger.error(errMsg, e);
            return null; // Todo : throw exception instead returning null
        }
    }

    private MessageDTO fromJson(String json, Class<MessageDTO> clazz) throws JsonProcessingException, IOException {
        return c_objMapper.readValue(json, clazz);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return obj == null ? null : c_objMapper.writeValueAsString(obj);
    }
}
