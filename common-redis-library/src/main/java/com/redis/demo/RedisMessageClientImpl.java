package com.redis.demo;

import com.redis.demo.dto.AgentMessageDTO;
import com.redis.demo.exception.MessagingException;
import io.lettuce.core.StreamMessage;

import java.util.List;

public class RedisMessageClientImpl implements RedisMessageClient<AgentMessageDTO>{

    private ConnectionFactory connectionFactory;

    public RedisMessageClientImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void produce(String streamName, AgentMessageDTO agentMessageDTO) throws MessagingException {

    }

    @Override
    public void createConsumerGroup(String agentAggregatorId, String agentId) throws MessagingException {

    }

    @Override
    public AgentMessageDTO read(String agentAggregatorId, String agentId, int count) throws MessagingException {
        return null;
    }

    @Override
    public void ack(String agentAggregatorId, String agentId, String messageId) throws MessagingException {

    }

    @Override
    public void delete(String agentAggregatorId, String agentId, String messageId) throws MessagingException {

    }

    @Override
    public int getPendingCount(String agentAggregatorId, String agentId) throws MessagingException {
        return 0;
    }

    @Override
    public List<StreamMessage<String, AgentMessageDTO>> readPendingMessage(String agentAggregatorId, String agentId, int count) throws MessagingException {
        return null;
    }

    @Override
    public void claim(String agentAggregatorId, String agentId, String sourceAgentId) throws MessagingException {

    }
}
