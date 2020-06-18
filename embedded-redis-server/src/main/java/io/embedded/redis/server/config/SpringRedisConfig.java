package io.embedded.redis.server.config;

import io.embedded.redis.server.serde.MessageSerializer;
import io.embedded.redis.server.serde.StringSerialization;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class SpringRedisConfig {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    // Lettuce Class
    @Bean(destroyMethod = "shutdown")
    ClientResources clientResources() {
        return DefaultClientResources.create();
    }

    // Lettuce Config
    @Bean
    public ClientOptions clientOptions(){
        return ClientOptions.builder()
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .autoReconnect(true)
            .build();
    }

    // Spring Data Redis
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        return new RedisStandaloneConfiguration(redisHost, redisPort); // By default host port is localhost and 6379
    }



    @Bean
    LettucePoolingClientConfiguration lettucePoolConfig(ClientOptions options, ClientResources dcr){
        return LettucePoolingClientConfiguration.builder()
            .poolConfig(new GenericObjectPoolConfig())
            .clientOptions(options)
            .clientResources(dcr)
            .build();
    }

    @Bean
    public RedisConnectionFactory connectionFactory(RedisStandaloneConfiguration redisStandaloneConfiguration,
                                                    LettucePoolingClientConfiguration lettucePoolConfig) {
        return new LettuceConnectionFactory(redisStandaloneConfiguration, lettucePoolConfig);
    }

    @Bean
//    @ConditionalOnMissingBean(name = "redisTemplate")
//    @Primary
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new MessageSerializer());
        return template;
    }
}
