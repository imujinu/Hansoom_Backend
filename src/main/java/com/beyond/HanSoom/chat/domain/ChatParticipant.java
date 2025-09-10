package com.beyond.HanSoom.chat.domain;

import com.beyond.HanSoom.common.domain.BaseTimeEntity;
import com.beyond.HanSoom.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="chat_room_id")
    private ChatRoom chatRoom;

    @Builder.Default
    private String isOnline = "N";

    private boolean exitYn;
    @Builder.Default
    private Long remaining = 0L;
    @Builder.Default
    private boolean participationYn = true;

    @Builder.Default
    private boolean isFirstEntry = true;
    @Column(columnDefinition = "TEXT")
    private String aesKey;
    @Column(columnDefinition = "TEXT")
    private String iv;
    public void updateOnlineState(String state){
        this.isOnline = state;
    }

    public void updateRemaining(Long remaining){
        this.remaining = remaining;
    }

    public void setKey(String aesKey, String iv){
        this.aesKey = aesKey.trim();
        this.iv = iv.trim();
    }


}
