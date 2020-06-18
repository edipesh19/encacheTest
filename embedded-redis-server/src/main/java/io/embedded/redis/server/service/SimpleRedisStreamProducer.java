package io.embedded.redis.server.service;

import io.embedded.redis.server.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class SimpleRedisStreamProducer {

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public SimpleRedisStreamProducer(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void produce(String streamName, MessageDTO messageDTO) {
        ObjectRecord<String, MessageDTO> record = StreamRecords.newRecord()
            .in(streamName)
            .ofObject(messageDTO);
        // XADD streamName * "_class" "com.oracle.dicom.agent.mediator.dto.AgentMessageDTO" "key1" "val1" "key2" "val2"
        RecordId recordId = redisTemplate.opsForStream().add(record);
        System.out.println("RecordId = " + recordId.getValue());
    }

}
