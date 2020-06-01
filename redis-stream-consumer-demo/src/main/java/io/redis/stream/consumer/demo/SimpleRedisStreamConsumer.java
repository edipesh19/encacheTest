package io.redis.stream.consumer.demo;

import io.lettuce.core.Consumer;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class SimpleRedisStreamConsumer {
    public String streamName;
    public String groupId;
    public String consumerId;
    public boolean delete;
    public int count;
    public boolean acks;
    public boolean readPending;
    public boolean claim;
    public String prevConsumerId;

    public SimpleRedisStreamConsumer(String streamName, String groupId, String consumerId, String count, String deleteOrNot,
                                     String acks, String readPending, String claim, String prevConsumerId) {
        this.streamName = streamName;
        this.groupId = groupId;
        this.consumerId = consumerId;
        this.count = Integer.parseInt(count);
        this.delete = Boolean.parseBoolean(deleteOrNot);
        this.acks = Boolean.parseBoolean(acks);
        this.readPending = Boolean.parseBoolean(readPending);
        this.claim = Boolean.parseBoolean(claim);
        this.prevConsumerId = prevConsumerId;
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

        if(claim) {
            Range<String> range = Range.create("-", "+");
            Limit limit = Limit.create(0, 10000000);
            List<Object> pending = syncCommands.xpending(streamName, Consumer.from(groupId, prevConsumerId),range, limit);
            System.out.println("Pending: " + pending);
            for(int i=0;i<pending.size();i++) {
                List<String> inner = (List<String>) pending.get(i);
                //System.out.println("**********" + inner.get(0));
                syncCommands.xclaim(streamName, Consumer.from(groupId, consumerId), 1000, inner.get(0));
            }
        }

        System.out.println("=========== Reading Pending ===========");
        if(readPending) {
            getPendingMessageIfAny(syncCommands);
        }
        System.out.println("=========== Done Reading Pending ===========");

        System.out.println("Waiting for new messages");

        while(true) {
            List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                Consumer.from(groupId, consumerId),
                XReadArgs.Builder.count(count)
                    .block(100),
                XReadArgs.StreamOffset.lastConsumed(streamName)
            );

            if (!messages.isEmpty()) {
                for (StreamMessage<String, String> message : messages) {
                    System.out.println(">>>>>>>>>>>>>>>>>>>>   " + message);
                    // Write processing logic
                    // Confirm that the message has been processed using XACK

                    if(acks) {
                        syncCommands.xack(streamName, groupId, message.getId());
                    }
                    if(delete) {
                        syncCommands.xdel(streamName, message.getId());
                    }
                }
                System.out.println("=================================== len = " + syncCommands.xlen(streamName));
            }
        }
    }


    private void getPendingMessageIfAny(RedisCommands<String, String> syncCommands) {
        while (true) {

            Range<String> range = Range.create("-", "+");
            Limit limit = Limit.create(0, 10000000);
            List<Object> pending = syncCommands.xpending(streamName, Consumer.from(groupId, consumerId), range, limit);
            System.out.println("Pending count: " + pending.size());
            if(pending.size() == 0) {
                break;
            }
            List<StreamMessage<String, String>> pendingMessages = syncCommands.xreadgroup(
                Consumer.from(groupId, consumerId),
                XReadArgs.Builder.count(count)
                    .block(5000),
                XReadArgs.StreamOffset.from(streamName, "0-0")
            );

            if (!pendingMessages.isEmpty()) {
                for (StreamMessage<String, String> message : pendingMessages) {
                    System.out.println("<<<<<<<<<<<<<<<<<<<<   " + message);
                    // Write processing logic
                    // Confirm that the message has been processed using XACK

                    syncCommands.xack(streamName, groupId, message.getId());
                    if(delete) {
                        syncCommands.xdel(streamName, message.getId());
                    }
                }
                System.out.println("=================================== len = " + syncCommands.xlen(streamName));
            }
        }
    }
}
