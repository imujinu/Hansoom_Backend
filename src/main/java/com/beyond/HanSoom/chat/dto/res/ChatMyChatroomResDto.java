package com.beyond.HanSoom.chat.dto.res;

import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.hotel.domain.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMyChatroomResDto {
    private Long roomId;
    private String hotelImage;
    private String hotelName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private String hotelAddress;
    private String isGroupChat;
    private Long unReadCount;
    private Long participants;
    @Builder.Default
    private String isOnline = "N";

    public ChatMyChatroomResDto fromEntity(ChatRoom chatRoom, Long unReadCount, String lastMessage, LocalDateTime lastMessageTime, String isOnline){
        Hotel hotel =chatRoom.getHotel();
        return ChatMyChatroomResDto.builder()
                .roomId(chatRoom.getId())
                .hotelImage(hotel.getImage())
                .hotelName(hotel.getHotelName())
                .hotelAddress(hotel.getAddress())
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .isOnline(isOnline)
                .participants((long) chatRoom.getParticipantList().size())
                .hotelName(chatRoom.getHotel().getHotelName())
                .isGroupChat(chatRoom.getIsGroupChat())
                .unReadCount(unReadCount)
                .build();
    }
}
