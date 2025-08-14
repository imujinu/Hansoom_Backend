package com.beyond.HanSoom.common.config;

import com.beyond.HanSoom.common.service.RedisStreamService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.util.ByteUtils;

import java.time.Duration;
import java.util.Objects;

@Configuration
public class RedisChatConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    // Redis Stream 전용 ConnectionFactory
    @Bean
    @Qualifier("redisStream")
    public RedisConnectionFactory redisStreamFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        // 필요하다면 configuration.setDatabase(dbIndex)로 DB 선택 가능
        return new LettuceConnectionFactory(configuration);
    }

    // Redis Stream 발행용 템플릿
    @Bean
    @Qualifier("redisStream")
    public StringRedisTemplate redisStreamTemplate(@Qualifier("redisStream") RedisConnectionFactory streamFactory) {
        return new StringRedisTemplate(streamFactory);
    }



}
