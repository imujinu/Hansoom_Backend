package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.dto.ChatMessageResDto;
import com.beyond.HanSoom.chat.repository.ChatMessageRepository;
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
    private final ChatService chatService;
    public ChatStreamListenerService(@Qualifier("redisStream") RedisTemplate<String, String> redisTemplate, SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
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
        String recordId = message.getId().getValue();
        ObjectMapper mapper = new ObjectMapper();
        ChatMessageResDto dto= null;
        try {
           dto = mapper.readValue(message.getValue(), ChatMessageResDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println(dto);
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, recordId);
        messagingTemplate.convertAndSend("/topic/" + dto.getRoomId(), dto);
        chatService.saveMessage(dto);

    }

}

