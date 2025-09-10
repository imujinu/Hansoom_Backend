package com.beyond.HanSoom.chat.domain;

import com.beyond.HanSoom.chat.dto.res.ChatMessageResDto;
import com.beyond.HanSoom.common.domain.BaseTimeEntity;
import com.beyond.HanSoom.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false )
    private User user;

    @Column(nullable = true, length = 500)
    private String content;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ChatReadStatus> readStatusList = new ArrayList<>();


    public ChatMessage toEntity(ChatMessageResDto dto, ChatRoom chatRoom, User user){
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .content(dto.getContent())
                .build();
    }
}
