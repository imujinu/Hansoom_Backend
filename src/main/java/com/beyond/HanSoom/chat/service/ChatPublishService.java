package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.dto.res.ChatMessageReqDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
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

    public RecordId publish(ChatMessageReqDto dto) {
        // payload를 JSON 문자열로 변환
        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // ObjectRecord 생성
        ObjectRecord<String, String> record = StreamRecords
                .newRecord()
                .ofObject(json)        // JSON 문자열을 value로
                .withStreamKey(streamKey);

        // Redis Stream에 추가
        RecordId recordId = redisTemplate.opsForStream().add(record);

        if (recordId == null) {
            throw new RuntimeException("Redis Stream record 처리 실패");
        }
        System.out.println("메시지 발행완료!!");
        return recordId;

    }
}

