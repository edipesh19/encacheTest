package com.redis.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.redis.demo.dto.AgentMessageDTO;
import com.redis.demo.exception.MessagingException;
import io.lettuce.core.Consumer;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisMessageClientImpl implements RedisMessageClient<AgentMessageDTO> {

    private ConnectionFactory connectionFactory;
    private StatefulRedisConnection<String, AgentMessageDTO> connection;
    private RedisCommands<String, AgentMessageDTO> syncCommands;

    public RedisMessageClientImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        connection = connectionFactory.getConnection("localhost", 6379);
        syncCommands = connection.sync();
    }

    @Override
    public void produce(String agentAggregatorId, String agentId, AgentMessageDTO agentMessageDTO) throws MessagingException {
        Map<String, AgentMessageDTO> messageBody = new HashMap<>();
        messageBody.put(agentMessageDTO.getMsgId(), agentMessageDTO);
        try {
            String redisMessageId = syncCommands.xadd(getstreamName(agentAggregatorId, agentId), messageBody);
            System.out.println("Message posted: " + redisMessageId);
            Thread.sleep(100);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void createConsumerGroup(String agentAggregatorId, String agentId) throws MessagingException {
        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.from(getstreamName(agentAggregatorId, agentId), "0-0"), agentAggregatorId);
        } catch (RedisBusyException redisBusyException) {
            System.out.println(String.format("\t Group '%s' already exists", agentAggregatorId));
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public List<StreamMessage<String, AgentMessageDTO>> readAsStream(String agentAggregatorId, String agentId, int count) throws MessagingException {
        try {
            List<StreamMessage<String, AgentMessageDTO>> messages = syncCommands.xreadgroup(
                Consumer.from(agentAggregatorId, agentId),
                XReadArgs.Builder.count(count)
                    .block(100),
                XReadArgs.StreamOffset.lastConsumed(getstreamName(agentAggregatorId, agentId))
            );
            return messages;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    private String getstreamName(String agentAggregatorId, String agentId) {
        return agentAggregatorId + agentId;
    }

    @Override
    public List<AgentMessageDTO> read(String agentAggregatorId, String agentId, int count) throws MessagingException {
        try {
            List<StreamMessage<String, AgentMessageDTO>> messages = syncCommands.xreadgroup(
                Consumer.from(agentAggregatorId, agentId),
                XReadArgs.Builder.count(count)
                    .block(100),
                XReadArgs.StreamOffset.lastConsumed(getstreamName(agentAggregatorId, agentId))
            );

            List<AgentMessageDTO> agentMessageDTOList = null;
            if (!messages.isEmpty()) {
                agentMessageDTOList = messages.stream().map(a -> a.getBody().values()).flatMap(c -> c.stream()).collect(Collectors.toList());
            /*for (StreamMessage<String, AgentMessageDTO> message : messages) {
                Collection<AgentMessageDTO> collection = message.getBody().values();
                agentMessageDTO.addAll(collection);
            }*/
            }
            return agentMessageDTOList;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void ack(String agentAggregatorId, String agentId, String messageId) throws MessagingException {
        try {
            syncCommands.xack(getstreamName(agentAggregatorId, agentId), agentAggregatorId, messageId);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void delete(String agentAggregatorId, String agentId, String messageId) throws MessagingException {
        try {
            syncCommands.xdel(getstreamName(agentAggregatorId, agentId), messageId);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public int getPendingCount(String agentAggregatorId, String agentId) throws MessagingException {
        try {
            List<Object> pending = syncCommands.xpending(getstreamName(agentAggregatorId, agentId),
                Consumer.from(agentAggregatorId, agentId),
                Range.create("-", "+"),
                Limit.create(0, Integer.MAX_VALUE));
            return pending.size();
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public List<StreamMessage<String, AgentMessageDTO>> readPendingMessage(String agentAggregatorId, String agentId, int count) throws MessagingException {
        try {
            List<Object> pending = syncCommands.xpending(getstreamName(agentAggregatorId, agentId),
                Consumer.from(agentAggregatorId, agentId),
                Range.create("-", "+"),
                Limit.create(0, 100));
            System.out.println("****Pending list: " + pending);

            List<StreamMessage<String, AgentMessageDTO>> pendingMessages = syncCommands.xreadgroup(
                Consumer.from(agentAggregatorId, agentId),
                XReadArgs.Builder.count(count)
                    .block(5000),
                XReadArgs.StreamOffset.from(getstreamName(agentAggregatorId, agentId), "0-0")
            );
            return pendingMessages;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public void claim(String agentAggregatorId, String agentId, String sourceAgentId) throws MessagingException {
        try {
            List<Object> pending = syncCommands.xpending(getstreamName(agentAggregatorId, sourceAgentId), Consumer.from(agentAggregatorId, sourceAgentId),
                Range.create("-", "+"),
                Limit.create(0, Integer.MAX_VALUE));
            System.out.println("+++++++Pending message for claiming: " + pending);
            for (int i = 0; i < pending.size(); i++) {
                List<String> inner = (List<String>) pending.get(i);
                System.out.println("****** Inner: " + inner.get(0));
                //syncCommands.xclaim(getstreamName(agentAggregatorId, sourceAgentId), Consumer.from(agentAggregatorId, agentId), 1000, inner.get(0));
            }
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }

    @Override
    public List<AgentMessageDTO> getMessageList(List<StreamMessage<String, AgentMessageDTO>> streamMessageList) throws MessagingException {
        try {
            List<AgentMessageDTO> agentMessageDTOList = null;
            if (!streamMessageList.isEmpty()) {
                agentMessageDTOList = streamMessageList.stream().map(a -> a.getBody().values()).flatMap(c -> c.stream()).collect(Collectors.toList());
            /*for (StreamMessage<String, AgentMessageDTO> message : messages) {
                Collection<AgentMessageDTO> collection = message.getBody().values();
                agentMessageDTO.addAll(collection);
            }*/
            }
            return agentMessageDTOList;
        } catch (Exception e) {
            System.out.println(e);
            throw new MessagingException(e.getMessage());
        }
    }
}
