package com.beyond.HanSoom.common.config;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.repository.ChatParticipantRepository;
import com.beyond.HanSoom.chat.service.ChatService;
import com.beyond.HanSoom.common.dto.SessionInfo;
import com.beyond.HanSoom.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompEventListener {
    private final Set<String> sessions = ConcurrentHashMap.newKeySet();
    private final ChatService chatService;

    @EventListener
    public void connectHandler(SessionConnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        log.info("connectId : " + accessor.getSessionId());
        log.info(String.valueOf(sessions.size()));
    }

    @EventListener
    public void subscribeHandle(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        SessionInfo sessionInfo = WebSocketSessionRegistry.get(sessionId);
        String email = sessionInfo.getEmail();
        String roomId = sessionInfo.getRoomId();
        chatService.updateOnlineState(email, roomId, "Y");

        log.info("Session Subscribe : " + sessionId + "roomId : " + roomId);
    }

    @EventListener
    public void disConnectHandler(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        SessionInfo sessionInfo = WebSocketSessionRegistry.get(sessionId);
//        String email = sessionInfo.getEmail();
//        String roomId = sessionInfo.getRoomId();
//
//        chatService.updateOnlineState(email,roomId,"N");
//
//        log.info("Session DisConnected : " + sessionId + "roomId : " + roomId);
//        WebSocketSessionRegistry.unregister(accessor.getSessionId());
        sessions.remove(accessor.getSessionId());
    }
}
