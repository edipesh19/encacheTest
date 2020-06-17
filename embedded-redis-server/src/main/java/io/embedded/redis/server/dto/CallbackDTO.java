package io.embedded.redis.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CallbackDTO {
    private String serviceName;

    private Methods httpMethod;

    private String callbackUrl;

    public enum Methods {
        PUT,
        POST
    }
}
