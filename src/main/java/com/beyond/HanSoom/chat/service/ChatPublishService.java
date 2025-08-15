package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatPublishService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${chat.stream-key}")
    private String streamKey;


    public ChatPublishService(@Qualifier("redisStream") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RecordId publish(ChatMessageDto dto){
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ObjectRecord<String, String> record = StreamRecords
                .newRecord()
                .ofObject(json)
                .withStreamKey(streamKey);

        return redisTemplate.opsForStream().add(record);
    }
}

