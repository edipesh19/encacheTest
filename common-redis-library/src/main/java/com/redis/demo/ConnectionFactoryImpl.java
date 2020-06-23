package com.redis.demo;

import com.redis.demo.dto.AgentMessageDTO;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

import java.time.Duration;

public class ConnectionFactoryImpl implements ConnectionFactory<String, AgentMessageDTO>{
    @Override
    public StatefulRedisConnection<String, AgentMessageDTO> getConnection(String host, int port) {
        RedisURI redisUri = RedisURI.Builder.redis(host).withPort(port).withTimeout(Duration.ofSeconds(30)).build();
        RedisClient client = RedisClient.create(redisUri);
        AgentMessageDTOCodec codec = new AgentMessageDTOCodec();
        return client.connect(codec);
    }
}
