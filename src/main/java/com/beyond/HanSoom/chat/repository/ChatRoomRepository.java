package com.beyond.HanSoom.chat.repository;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    ChatRoom findByHotelAndIsGroupChat(Hotel hotel, String y);

    List<ChatRoom> findAllByIsGroupChat(String y);


    ChatRoom findByReservationAndIsGroupChat(Reservation r, String n);

    ChatRoom findByHotelAndReservationAndIsGroupChat(Hotel hotel, Reservation reservation, String n);

    List<ChatRoom> findAllByHotelAndIsGroupChat(Hotel hotel, String n);
}
