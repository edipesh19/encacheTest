package io.embedded.redis.server.resource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.embedded.redis.server.dto.MessageDTO;
import io.embedded.redis.server.service.SimpleRedisStreamProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ProducerResource {

    private static final Logger logger = LoggerFactory.getLogger(ProducerResource.class.getName());
    private static ObjectMapper c_objMapper;
    private final SimpleRedisStreamProducer producer;

    @Autowired
    public ProducerResource(SimpleRedisStreamProducer producer) {
        this.producer = producer;
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @PostMapping("send/{streamName}")
    public MessageDTO send(@PathVariable("streamName") String streamName, @RequestBody MessageDTO messageDTO) {
        messageDTO.setMsgId(UUID.randomUUID().toString());
        producer.produce(streamName, messageDTO);

        return messageDTO;
    }
}
