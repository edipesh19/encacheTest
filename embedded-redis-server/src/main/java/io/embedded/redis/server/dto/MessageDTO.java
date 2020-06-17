package io.embedded.redis.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageDTO {

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
