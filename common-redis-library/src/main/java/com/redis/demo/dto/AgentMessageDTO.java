package com.redis.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AgentMessageDTO implements IBaseAgentMessage {

    private String msgId;
    private long lastUpdateTime;
    private String aggregatorId;
    private String agentId;
    private String agentInstanceId;
    private CallbackDTO callbackDto;
    private String msgType;
    private String payload;
    private String payloadType;

}
