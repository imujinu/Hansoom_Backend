package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.dto.res.ChatMessageResDto;
import com.beyond.HanSoom.chat.repository.ChatParticipantRepository;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
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
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
//    @Value("${POD_NAME:local}")
//    private String podName;
    public ChatStreamListenerService(@Qualifier("redisStream") RedisTemplate<String, String> redisTemplate, SimpMessagingTemplate messagingTemplate, ChatService chatService, UserRepository userRepository, ChatParticipantRepository chatParticipantRepository, ChatRoomRepository chatRoomRepository) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRoomRepository = chatRoomRepository;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
//        this.consumerGroupName = "chat-group-" + podName;
        createStreamConsumerGroup(streamKey, consumerGroupName);

        listenerContainer = StreamMessageListenerContainer.create(
                redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .targetType(String.class)
                        .pollTimeout(Duration.ofMillis(100))
                        .errorHandler(t -> {
                            System.err.println("Stream listener 에러 발생: " + t.getMessage());
                            // 재시작
                            if (!listenerContainer.isRunning()) {
                                listenerContainer.start();
                            }
                        })
                        .build()
        );



        subscribeStream();

        listenerContainer.start();

    }
    private void subscribeStream() {
        if (subscription != null) {
            subscription.cancel(); // 기존 구독 취소
        }
        subscription = listenerContainer.receive(
                Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );
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
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        ChatMessageResDto dto= null;
        try {
            dto = mapper.readValue(message.getValue(), ChatMessageResDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        User user = userRepository.findByEmail(dto.getSenderEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        dto.setIsGroupChat(chatRoom.getIsGroupChat());
        ChatParticipant me = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅 유저 입니다."));
        User hostUser = chatRoom.getHotel().getUser();
        dto.updateUser(user);
        if(dto.isWaring()){
            ChatRoom chatroom = chatRoomRepository.findById(dto.getRoomId()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채팅방입니다."));
            ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatroom,user).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채팅 참여자 입니다."));
            System.out.println("채팅 제한 시간 =========" + dto.getRemaining());
            chatParticipant.updateRemaining(dto.getRemaining());
            chatParticipantRepository.save(chatParticipant);
        }
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, recordId);
        messagingTemplate.convertAndSend("/topic/" + dto.getRoomId(), dto);
        System.out.println("메시지 수신 완료" + dto.getRoomId());
        chatService.saveMessage(dto);

    }

}