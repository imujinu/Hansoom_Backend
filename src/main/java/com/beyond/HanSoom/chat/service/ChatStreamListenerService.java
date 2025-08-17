package com.beyond.HanSoom.chat.service;

import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ChatStreamListenerService  implements InitializingBean, StreamListener<String, ObjectRecord<String, Map>> {
    private final RedisTemplate<String,String> redisTemplate;
    @Value("${chat.stream-key}")
    private String streamKey;
    @Value("${chat.group}")
    private String consumerGroupName;
    @Value("${chat.consumer-name}")
    private String consumerName;
    private final SimpMessagingTemplate messagingTemplate;
    private StreamMessageListenerContainer<String, ObjectRecord<String, Map>> listenerContainer;

    private Subscription subscription;

    public ChatStreamListenerService(@Qualifier("redisStream") RedisTemplate<String, String> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createStreamConsumerGroup(streamKey, consumerGroupName);

        this.listenerContainer = StreamMessageListenerContainer.create(
                redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .targetType(Map.class)
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


    public void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
        try {
            // 스트림 키가 없으면 먼저 메시지를 하나 추가해서 스트림 생성
            if (!redisTemplate.hasKey(streamKey)) {
                redisTemplate.opsForValue().set(streamKey, "init"); // 임시 값 추가
            }

            // Consumer Group 존재 여부 확인 후 없으면 생성
            if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
                redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private boolean isStreamConsumerGroupExist(String streamKey, String groupName) {
        try {
            StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamKey);
            if (groups != null) {
                for (StreamInfo.XInfoGroup group : groups) {
                    if (groupName.equals(group.groupName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void onMessage(ObjectRecord<String, Map> message) {
        Map<String, String> body = message.getValue();
        String roomId = body.get("roomId");
        String sendMessage = body.get("message");
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, sendMessage);
    }
}

