package com.beyond.HanSoom.chat.domain;

import com.beyond.HanSoom.common.domain.BaseTimeEntity;
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
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Builder.Default
    private String isGroupChat="N";

    //하나의 채팅방에는 다수의 참여자
    // 다수의 메세지가 있음
    //근데 채팅방이 사라지면 모두 삭제되어야 하므로 연결해놓음
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ChatParticipant> participantList = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom" , cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> chatMessageList = new ArrayList<>();
}
