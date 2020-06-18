package io.embedded.redis.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmbeddedRedisServer {
    public static void main(String[] args) {
        SpringApplication.run(EmbeddedRedisServer.class, args);
    }
}
