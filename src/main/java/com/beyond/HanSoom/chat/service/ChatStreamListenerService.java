package com.beyond.HanSoom.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class ChatStreamListenerService  implements InitializingBean, StreamListener<String, ObjectRecord<String, String>> {
    private final RedisTemplate<String,String> redisTemplate;
    @Value("${chat.stream-key}")
    private String streamKey;
    @Value("${chat.group}")
    private String consumerGroupName;
    @Value("${chat.consumer-name}")
    private String consumerName;
    private final SimpMessagingTemplate messagingTemplate;
    private StreamMessageListenerContainer<String, ObjectRecord<String, String>> listenerContainer;

    private Subscription subscription;

    public ChatStreamListenerService(@Qualifier("redisStream") RedisTemplate<String, String> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        createStreamConsumerGroup(streamKey, consumerGroupName);

        listenerContainer = StreamMessageListenerContainer.create(
                redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .targetType(String.class)
                        .pollTimeout(Duration.ofSeconds(1))
                        .build()
        );
        System.out.println("=========listener 객체 확인 ========");
        System.out.println("Listener Container isRunning: " + listenerContainer.isRunning());


        subscription = listenerContainer.receive(
                Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );

        System.out.println("=========subscription 객체 확인 ========");

        listenerContainer.start();
        System.out.println("Subscription active:  "+subscription.isActive());
        System.out.println("컨테이너 시작됨");

    }


    public void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
        try {
            // 스트림 키가 없으면 먼저 메시지를 하나 추가해서 스트림 생성
            if (!redisTemplate.hasKey(streamKey)) {
                redisTemplate.opsForStream().add(
                        StreamRecords.newRecord()
                                .ofMap(Map.of("init", "0"))
                                .withStreamKey(streamKey)
                );
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
    public void onMessage(ObjectRecord<String, String> message) {
        String stream = message.getStream();
        String recordId = message.getId().getValue();

        System.out.println("isMessage! connected");
        String value = message.getValue(); // 스트림에 저장된 문자열
        System.out.println("Raw message: " + value);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String sendMessage = node.get("message").asText();
        String roomId = node.get("roomId").asText();
        System.out.println(sendMessage);
        this.redisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, recordId);
        messagingTemplate.convertAndSend("/topic/" + roomId, sendMessage);
    }

}

