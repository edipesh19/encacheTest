package io.embedded.redis.server.embedded;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

@Component
public class EmbeddedRedisService {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedRedisService.class.getName());

    private RedisServer redisServer;

//    @Value("${redis.port}:6379")
//    private int redisPort;

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }

    @PostConstruct
    public void start() throws IOException {
        Thread t = new Thread(() -> {
            File redisServerFile = new File("/Users/dipdutta/Downloads/redis-6.0.1/src/redis-server");
            redisServer = new RedisServer(redisServerFile, 6379);
            redisServer.start();
        });
        t.start();
    }
}
