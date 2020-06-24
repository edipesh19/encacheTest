package com.redis.demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

import java.time.Duration;

public class ConnectionFactoryImpl implements ConnectionFactory<String, String>{
    @Override
    public StatefulRedisConnection<String, String> getConnection(String host, int port) {
        RedisURI redisUri = RedisURI.Builder.redis(host).withPort(port).withTimeout(Duration.ofSeconds(30)).build();
        RedisClient client = RedisClient.create(redisUri);
        return client.connect();
    }
}
