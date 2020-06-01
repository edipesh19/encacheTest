package io.redis.stream.consumer.demo;

import io.lettuce.core.Consumer;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;

public class SimpleRedisStreamConsumer {
    public String streamName;
    public String groupId;
    public String consumerId;

    public SimpleRedisStreamConsumer(String streamName, String groupId, String consumerId) {
        this.streamName = streamName;
        this.groupId = groupId;
        this.consumerId = consumerId;
    }

    public void consume() {
        RedisClient redisClient = RedisClient.create("redis://localhost:6379"); // change to reflect your environment
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.from(streamName, "0-0"), groupId);
        } catch (RedisBusyException redisBusyException) {
            System.out.println(String.format("\t Group '%s' already exists", groupId));
        }

        System.out.println("Waiting for new messages");

        while (true) {

            List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                Consumer.from(groupId, consumerId),
                XReadArgs.StreamOffset.lastConsumed(streamName)
            );

            if (!messages.isEmpty()) {
                for (StreamMessage<String, String> message : messages) {
                    System.out.println(">>>>>>>>>>>>>>>>>>>>   " + message);
                    // Confirm that the message has been processed using XACK
                    syncCommands.xack(streamName, groupId, message.getId());
                }
            }
        }
    }
}
