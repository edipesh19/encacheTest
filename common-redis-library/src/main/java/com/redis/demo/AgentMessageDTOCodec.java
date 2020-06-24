package com.redis.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.demo.dto.AgentMessageDTO;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AgentMessageDTOCodec implements RedisCodec<String, AgentMessageDTO> {

    private static ObjectMapper c_objMapper;
    private static StringCodec stringCodec;
    private final String encoding = "UTF8";
    private final Charset charset = StandardCharsets.UTF_8;

    public AgentMessageDTOCodec() {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        stringCodec = new StringCodec();

    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return obj == null ? null : c_objMapper.writeValueAsString(obj);
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        /*try {
            if (bytes == null) {
                return null;
            } else {
                String data;
                if (bytes.hasArray()) {
                    data = new String(bytes.array(),
                        bytes.arrayOffset() + bytes.position(),
                        bytes.remaining(), this.encoding);
                } else {
                    final byte[] b = new byte[bytes.remaining()];
                    bytes.duplicate().get(b);
                    data = new String(b, this.encoding);
                }
                return data;
            }
        } catch (Exception e) {
            return null; // Todo : throw exception instead returning null
        }*/
        //return charset.decode(bytes).toString();
        return stringCodec.decodeKey(bytes);
    }

    @Override
    public AgentMessageDTO decodeValue(ByteBuffer bytes) {
        try {
            if (bytes == null) {
                return null;
            } else {
                String data;
                if (bytes.hasArray()) {
                    data = new String(bytes.array(),
                        bytes.arrayOffset() + bytes.position(),
                        bytes.remaining(), this.encoding);
                } else {
                    final byte[] b = new byte[bytes.remaining()];
                    bytes.duplicate().get(b);
                    data = new String(b, this.encoding);
                }
                return c_objMapper.readValue(data, AgentMessageDTO.class);
            }
        } catch (Exception e) {
            return null; // Todo : throw exception instead returning null
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        /*try {
            if (key == null) {
                return null;
            } else {
                return ByteBuffer.wrap(toJson(key).getBytes(encoding));
            }
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            //String errMsg = "Error when serializing AgentMessageDTO to byte[]";
            //logger.error(errMsg, e);
            return null; // Todo : throw exception instead returning null
        }*/
        //return charset.encode(key);
        return stringCodec.encodeKey(key);
    }

    @Override
    public ByteBuffer encodeValue(AgentMessageDTO messageDTO) {
        try {
            if (messageDTO == null) {
                return null;
            } else {
                return ByteBuffer.wrap(toJson(messageDTO).getBytes(encoding));
            }
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            //String errMsg = "Error when serializing AgentMessageDTO to byte[]";
            //logger.error(errMsg, e);
            return null; // Todo : throw exception instead returning null
        }
    }
}
