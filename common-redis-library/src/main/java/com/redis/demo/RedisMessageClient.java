package com.redis.demo;

import com.redis.demo.dto.AgentMessageDTO;
import com.redis.demo.exception.MessagingException;
import io.lettuce.core.StreamMessage;

import java.util.List;

public interface RedisMessageClient<T> {
    void produce(String streamName, T object) throws MessagingException;
    void createConsumerGroup(String streamName, String agentAggregatorId) throws MessagingException;
    List<StreamMessage<String, String>> readAsStream(String streamName, String agentAggregatorId, String agentId, int count) throws MessagingException;
    void ack(String streamName, String agentAggregatorId, String messageId) throws MessagingException;
    void delete(String streamName, String messageId) throws MessagingException;
    int getPendingCount(String streamName, String agentAggregatorId, String agentId) throws MessagingException;
    List<StreamMessage<String, String>> readPendingMessage(String streamName, String agentAggregatorId, String agentId, int count) throws MessagingException;
    void claim(String sourceStreamName, String sourceAgentId, String agentAggregatorId, String agentId) throws MessagingException;
    List<T> getMessageList(List<StreamMessage<String, String>> streamMessageList) throws MessagingException;
}
