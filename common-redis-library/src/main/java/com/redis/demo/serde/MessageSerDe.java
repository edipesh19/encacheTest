package com.redis.demo.serde;

import com.redis.demo.dto.AgentMessageDTO;

public interface MessageSerDe {
    String serialize(AgentMessageDTO object);
    AgentMessageDTO deserialize(String string);
}
