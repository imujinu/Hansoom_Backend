package com.beyond.HanSoom.common.config;

import com.beyond.HanSoom.chat.service.ChatService;
import com.beyond.HanSoom.common.dto.SessionInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final ChatService chatService;
    @Value("${jwt.secretKeyAt}")
    private String secretKey;


    //connect, subscribe, disconnect 요청 시에 presend 메서드가 실행됨
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //Stomp안에 변수값에 쉽게 접근하기 위해 message를 감싸준다.
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        //요청이 무엇인지 getCommand에 담겨있음
        if(StompCommand.CONNECT == accessor.getCommand()){

            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            String destination = accessor.getDestination(); // ex: /topic/room1
            String roomId = destination.split("/")[2];

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            SessionInfo sessionInfo = SessionInfo.builder()
                    .email(claims.getSubject())
                    .roomId(roomId)
                    .build();
            WebSocketSessionRegistry.register(accessor.getSessionId(), sessionInfo);

        }
        if(StompCommand.DISCONNECT == accessor.getCommand()){
            String bearerToken = accessor.getFirstNativeHeader("Authorization");


        }


    return message;
    }
}
