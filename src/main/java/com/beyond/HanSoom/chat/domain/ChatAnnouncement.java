package com.beyond.HanSoom.chat.domain;

import com.beyond.HanSoom.common.domain.BaseTimeEntity;
import com.beyond.HanSoom.hotel.domain.Hotel;
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
public class ChatAnnouncement  extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;
    private String isActive;
    private String chatType;

    public void changeActive(String isActive){
        this.isActive = isActive;
    }

}
