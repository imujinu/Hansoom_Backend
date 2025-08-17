package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatPublishService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${chat.stream-key}")
    private String streamKey;


    public ChatPublishService(@Qualifier("redisStream") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RecordId publish(ChatMessageDto dto) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("roomId", String.valueOf(dto.getRoomId()));        // 채팅방 구분
        messageMap.put("senderId", String.valueOf(dto.getSenderId()));     // 발신자 ID
        messageMap.put("message", dto.getMessage()); // 실제 메시지 내용
        messageMap.put("timestamp", String.valueOf(dto.getTimestamp())); // 선택적: 시간
        MapRecord<String, String, String> record = StreamRecords
                .newRecord()
                .ofMap(messageMap)
                .withStreamKey(streamKey);

        return redisTemplate.opsForStream().add(record);
    }
}

