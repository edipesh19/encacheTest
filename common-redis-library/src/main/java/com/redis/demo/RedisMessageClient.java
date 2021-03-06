package com.redis.demo;

import com.redis.demo.dto.AgentMessageDTO;
import com.redis.demo.exception.MessagingException;
import io.lettuce.core.StreamMessage;

import java.util.List;

public interface RedisMessageClient<T> {
    void produce(String agentAggregatorId, String agentId, T agentMessageDTO) throws MessagingException;
    void createConsumerGroup(String agentAggregatorId, String agentId) throws MessagingException;
    List<StreamMessage<String, T>> readAsStream(String agentAggregatorId, String agentId, int count) throws MessagingException;
    List<T> read(String agentAggregatorId, String agentId, int count) throws MessagingException;
    void ack(String agentAggregatorId, String agentId, String messageId) throws MessagingException;
    void delete(String agentAggregatorId, String agentId, String messageId) throws MessagingException;
    int getPendingCount(String agentAggregatorId, String agentId) throws MessagingException;
    List<StreamMessage<String, T>> readPendingMessage(String agentAggregatorId, String agentId, int count) throws MessagingException;
    void claim(String agentAggregatorId, String agentId, String sourceAgentId) throws MessagingException;
    List<T> getMessageList(List<StreamMessage<String, T>> streamMessageList) throws MessagingException;
}
