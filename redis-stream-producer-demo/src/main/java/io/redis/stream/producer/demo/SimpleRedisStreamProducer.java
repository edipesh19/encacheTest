package io.redis.stream.producer.demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class SimpleRedisStreamProducer {

    private String redisUrl;
    private RedisClient redisClient;

    public SimpleRedisStreamProducer(String redisUrl) {
        this.redisUrl = redisUrl;
        this.redisClient = RedisClient.create(redisUrl);

    }

    public void produce(String streamName, String message) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        send(syncCommands, streamName, message);
        System.out.println("\n");
        connection.close();
        redisClient.shutdown();
    }

    private void send(RedisCommands<String, String> syncCommands, String streamName, String message) {

        String messageId = syncCommands.xadd(
            streamName,
            message);
    }

}
