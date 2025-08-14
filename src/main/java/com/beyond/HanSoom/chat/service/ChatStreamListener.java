package com.beyond.HanSoom.chat.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

public class ChatStreamListener implements InitializingBean, StreamListener<String, ObjectRecord<String,String>> {
    private StringRedisTemplate redisTemplate;

    // Stream 관련 필드
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;
    private Subscription subscription;

    private final String streamKey = "chat:room:*";   // 예: 채팅방 스트림 키
    private final String consumerGroupName = "chatGroup";
    private final String consumerName = "consumer1";

    public void ChatStreamListenerService(@Qualifier("redisStream") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createStreamConsumerGroup(streamKey, consumerGroupName);

        this.listenerContainer = StreamMessageListenerContainer.create(
                redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .targetType(String.class)
                        .pollTimeout(Duration.ofSeconds(2))
                        .build()
        );

        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );

        this.listenerContainer.start();
    }

    private void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
        if (!redisTemplate.hasKey(streamKey)) {
            // Stream 없으면 생성하면서 Group 만들기
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
        } else {
            // Stream은 존재하지만 Group 없으면 생성
                redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
        }
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {

    }
}
