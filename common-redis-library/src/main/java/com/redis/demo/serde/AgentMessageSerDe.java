package com.redis.demo.serde;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.demo.dto.AgentMessageDTO;

public class AgentMessageSerDe implements MessageSerDe {

    private static ObjectMapper c_objMapper;

    public AgentMessageSerDe() {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String serialize(AgentMessageDTO object) {
        try {
            if (object == null) {
                return null;
            } else {
                String result = c_objMapper.writeValueAsString(object);
                return result;
            }
        } catch (JsonProcessingException e) {
            String errMsg = "Error when serializing AgentMessageDTO to String";
            System.out.println(errMsg);
            return null;
        }
    }

    @Override
    public AgentMessageDTO deserialize(String string) {
        try {
            if (string == null) {
                return null;
            } else {
                return c_objMapper.readValue(string, AgentMessageDTO.class);
            }
        } catch (IOException e) {
            String errMsg = "Error when DeSerializing String to AgentMessageDTO";
            e.printStackTrace();
            System.out.println(errMsg);
            return null;
        }
    }
}
