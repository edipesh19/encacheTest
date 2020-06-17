package io.embedded.redis.server.service;

import io.embedded.redis.server.dto.Message;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class SimpleRedisStreamProducer {

    private RedisClient redisClient;
    public static final long DEFAULT_TIMEOUT = 60;

    public SimpleRedisStreamProducer(@Value("${redis.host}") String redisHost, @Value("${redis.port}") String redisPort) {
        RedisURI redisURI = new RedisURI(redisHost, Integer.parseInt(redisPort), Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.redisClient = RedisClient.create(redisURI);
    }

    public void produce(String streamName, Message message) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        send(syncCommands, streamName, message);
        connection.close();
    }

    private void send(RedisCommands<String, String> syncCommands, String streamName, Message message) {
        Map<String, String> msg = new HashMap<>();
        msg.put(message.getMessageId(), message.getMessage());
        String messageId = syncCommands.xadd(
            streamName,
            msg);
    }


    @PreDestroy
    public void destroy() {
        redisClient.shutdown();
    }

}
