package io.redis.stream.consumer.demo;

import java.util.List;
import com.redis.demo.ConnectionFactoryImpl;
import com.redis.demo.RedisMessageClient;
import com.redis.demo.RedisMessageClientImpl;
import com.redis.demo.exception.MessagingException;
import io.lettuce.core.StreamMessage;

public class SimpleRedisStreamConsumer {
    private String sourceStreamName;
    private String streamName;
    private String consumerId;
    public boolean delete;
    public int count;
    public boolean acks;
    public boolean readPending;
    public boolean claim;
    public String sourceAgentId;
    private RedisMessageClient redisMessageClient;

    public SimpleRedisStreamConsumer(String count, String deleteOrNot,
                                     String acks, String readPending, String claim, String sourceAgentId, String agentId, String streamName, String sourceStreamName) {
        redisMessageClient = new RedisMessageClientImpl(new ConnectionFactoryImpl());
        this.count = Integer.parseInt(count);
        this.delete = Boolean.parseBoolean(deleteOrNot);
        this.acks = Boolean.parseBoolean(acks);
        this.readPending = Boolean.parseBoolean(readPending);
        this.claim = Boolean.parseBoolean(claim);
        this.sourceAgentId = sourceAgentId;
        this.consumerId = agentId;
        this.streamName = streamName;
        this.sourceStreamName = sourceStreamName;
    }

    public void consume() throws MessagingException {
        redisMessageClient.createConsumerGroup(streamName, "aggregator1");
        if (claim) {
            redisMessageClient.claim(sourceStreamName, sourceAgentId, "aggregator1", consumerId);
        }
        if (readPending) {
            int pendingCount = redisMessageClient.getPendingCount(streamName,"aggregator1", consumerId);
            System.out.println("----------- Pending count: " + pendingCount);
            if (pendingCount > 0) {
                List<StreamMessage<String, String>> pendingMessageList = redisMessageClient.readPendingMessage(streamName, "aggregator1", consumerId, pendingCount);
                System.out.println("******** AgentMessageDTOList" + redisMessageClient.getMessageList(pendingMessageList));

                for (int i=0; i< pendingMessageList.size(); i++) {
                    redisMessageClient.ack(streamName, "aggregator1", pendingMessageList.get(i).getId());
                }
            }
        }
        System.out.println("=========== Done Reading Pending ===========");

        System.out.println("Waiting for new messages");
        while (true) {
            List<StreamMessage<String, String>> streamMessageList = redisMessageClient.readAsStream(streamName, "aggregator1", consumerId, count);
            System.out.println("******* AgentMessageDTOList: " + redisMessageClient.getMessageList(streamMessageList));

            if (acks) {
                for (int i=0; i< streamMessageList.size(); i++) {
                    redisMessageClient.ack(streamName,"aggregator1", streamMessageList.get(i).getId());
                }
            }

            if (delete) {
                for (int i=0; i< streamMessageList.size(); i++) {
                    redisMessageClient.delete(streamName, streamMessageList.get(i).getId());
                }
            }
        }
    }
}
