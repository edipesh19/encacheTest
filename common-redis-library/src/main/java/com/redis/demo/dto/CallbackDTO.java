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
public class CallbackDTO {
    private String serviceName;

    private Methods httpMethod;

    private String callbackUrl;

    public enum Methods {
        PUT,
        POST
    }
}
