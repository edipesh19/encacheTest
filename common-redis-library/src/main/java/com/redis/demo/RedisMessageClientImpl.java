package com.redis.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.redis.demo.dto.AgentMessageDTO;
import com.redis.demo.exception.MessagingException;
import com.redis.demo.serde.AgentMessageSerDe;
import com.redis.demo.serde.MessageSerDe;
import io.lettuce.core.Consumer;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XGroupCreateArgs;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisMessageClientImpl implements RedisMessageClient<AgentMessageDTO> {

    private ConnectionFactory connectionFactory;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private MessageSerDe agentMessageSerDe;

    public RedisMessageClientImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        connection = connectionFactory.getConnection("localhost", 6379);
        syncCommands = connection.sync();
        agentMessageSerDe = new AgentMessageSerDe();
    }

    @Override
    public void produce(String streamName, AgentMessageDTO message) throws MessagingException {
        Map<String, String> messageBody = new HashMap<>();
        messageBody.put(message.getMsgId(), agentMessageSerDe.serialize(message));
        try {
            String redisMessageId = syncCommands.xadd(streamName, messageBody);
            System.out.println("Message posted: " + redisMessageId);
            Thread.sleep(100);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void createConsumerGroup(String streamName, String agentAggregatorId) throws MessagingException {
        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.from(streamName, "0-0"), agentAggregatorId,
                XGroupCreateArgs.Builder.mkstream());
        } catch (RedisBusyException redisBusyException) {
            System.out.println(String.format("\t Group '%s' already exists", agentAggregatorId));
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public List<StreamMessage<String, String>> readAsStream(String streamName, String agentAggregatorId, String agentId, int count) throws MessagingException {
        try {
            List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                Consumer.from(agentAggregatorId, agentId),
                XReadArgs.Builder.count(count)
                    .block(100),
                XReadArgs.StreamOffset.lastConsumed(streamName)
            );
            return messages;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void ack(String streamName, String agentAggregatorId, String messageId) throws MessagingException {
        try {
            syncCommands.xack(streamName, agentAggregatorId, messageId);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void delete(String streamName, String messageId) throws MessagingException {
        try {
            syncCommands.xdel(streamName, messageId);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public int getPendingCount(String streamName, String agentAggregatorId, String agentId) throws MessagingException {
        try {
            List<Object> pending = syncCommands.xpending(streamName,
                Consumer.from(agentAggregatorId, agentId),
                Range.create("-", "+"),
                Limit.create(0, Integer.MAX_VALUE));
            return pending.size();
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public List<StreamMessage<String, String>> readPendingMessage(String streamName, String agentAggregatorId, String agentId, int count) throws MessagingException {
        try {
            List<StreamMessage<String, String>> pendingMessages = syncCommands.xreadgroup(
                Consumer.from(agentAggregatorId, agentId),
                XReadArgs.Builder.count(count)
                    .block(5000),
                XReadArgs.StreamOffset.from(streamName, "0-0")
            );
            return pendingMessages;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void claim(String sourceStreamName, String sourceAgentId, String agentAggregatorId, String agentId) throws MessagingException {
        try {
            List<Object> pending = syncCommands.xpending(sourceStreamName, Consumer.from(agentAggregatorId, sourceAgentId),
                Range.create("-", "+"),
                Limit.create(0, Integer.MAX_VALUE));
            System.out.println("+++++++Pending message for claiming: " + pending);
            for (int i = 0; i < pending.size(); i++) {
                List<String> inner = (List<String>) pending.get(i);
                System.out.println("****** Inner: " + inner.get(0));
                syncCommands.xclaim(sourceStreamName, Consumer.from(agentAggregatorId, agentId), 1000, inner.get(0));
            }
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public List<AgentMessageDTO> getMessageList(List<StreamMessage<String, String>> streamMessageList) throws MessagingException {
        try {
            List<AgentMessageDTO> agentMessageDTOList = new ArrayList<>();
            if (streamMessageList != null && !streamMessageList.isEmpty()) {
                //agentMessageDTOList = streamMessageList.stream().map(a -> a.getBody().values()).flatMap(c -> c.stream().forEach(s -> agentMessageSerDe.deserialize(s))).collect(Collectors.toList());
                for (StreamMessage<String, String> message : streamMessageList) {
                    Collection<String> collection = message.getBody().values();
                    List<AgentMessageDTO> list = collection.stream().map(s -> agentMessageSerDe.deserialize(s)).collect(Collectors.toList());
                    agentMessageDTOList.addAll(list);
                }
            }
            return agentMessageDTOList;
        } catch (Exception e) {
            System.out.println(e);
            throw new MessagingException(e.getMessage());
        }
    }

    public void setAgentMessageSerDe(MessageSerDe agentMessageSerDe) {
        this.agentMessageSerDe = agentMessageSerDe;
    }
}
