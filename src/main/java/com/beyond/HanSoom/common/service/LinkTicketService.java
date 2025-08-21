package com.beyond.HanSoom.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class LinkTicketService {

    private static final String KEY_NS = "LINK:TICKET:";   // 네임스페이스
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public LinkTicketService(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String createTicket(LinkTicketPayload payload) {
        String ticket = UUID.randomUUID().toString();
        String key = KEY_NS + ticket;
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(key, json, TTL); // TTL 설정
            return ticket;
        } catch (Exception e) {
            throw new IllegalStateException("linkTicket 생성 실패", e);
        }
    }

    public LinkTicketPayload consumeTicket(String ticket) {
        String key = KEY_NS + ticket;
        String json = redisTemplate.opsForValue().getAndDelete(key); // 원자적 GET+DEL
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, LinkTicketPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("linkTicket 페이로드 파싱 실패", e);
        }
    }
}

