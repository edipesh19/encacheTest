package com.redis.demo;

import io.lettuce.core.api.StatefulRedisConnection;

public interface ConnectionFactory<String, T> {
    StatefulRedisConnection<String, T> getConnection(String host, int port);
}
