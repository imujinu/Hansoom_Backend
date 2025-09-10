package com.beyond.HanSoom.chat.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRateLimiter {

    private final Map<String, List<Long>> userMessageTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> userBlockedUntil = new ConcurrentHashMap<>();
    private static final int LIMIT = 20; // 1분에 60회
    private static final long WINDOW_MS = 60_000; // 1분
    private static final long BLOCK_MS = 1 * 60_000; // 5분 차단

    public boolean canSendMessage(String email) {
        long now = System.currentTimeMillis();

        if (userBlockedUntil.containsKey(email) && userBlockedUntil.get(email) <= now) {
            userBlockedUntil.remove(email);
            userMessageTimes.remove(email);
        }
        // 차단 중인지 확인
        if (userBlockedUntil.containsKey(email) && userBlockedUntil.get(email) > now) {
            return false;
        }

        userMessageTimes.putIfAbsent(email, new ArrayList<>());
        List<Long> times = userMessageTimes.get(email);
        times.removeIf(t -> t < now - WINDOW_MS); // 1분 이전 메시지 제거

        if (times.size() >= LIMIT) {
            userBlockedUntil.put(email, now + BLOCK_MS);
            return false;
        }

        times.add(now);
        return true;
    }

    public long getBlockedRemaining(String email) {
        long now = System.currentTimeMillis();
        long blockedUntil = userBlockedUntil.getOrDefault(email, 0L);
        return Math.max(0, blockedUntil);
    }
}