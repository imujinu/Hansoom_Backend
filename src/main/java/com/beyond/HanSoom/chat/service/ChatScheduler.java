package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.repository.ChatParticipantRepository;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatScheduler {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReservationRepository reservationRepository;
    // 초 분 시 일 월 요일
    // cron = "0 0 0 * * *" 매일 정오 마다 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void groupRoomCheckUser(){
        log.info("스케줄러 시작");
        LocalDate now = LocalDate.now();
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByIsGroupChat("Y");
        for(ChatRoom cr : chatRoomList){
            List<ChatParticipant> chatParticipants = cr.getParticipantList();
            Hotel hotel = cr.getHotel();

            Iterator<ChatParticipant> iter = chatParticipants.iterator();
            while(iter.hasNext()){
                ChatParticipant cp = iter.next();
                User user = cp.getUser();
                List<Reservation> reservation = reservationRepository.findAllByUserAndHotel(user, hotel);

                boolean hasOngoingReservation = reservation.stream()
                        //체크인 날짜가 현재 이전이고 체크아웃 날짜가 현재보다 이후일 떄 = now가 chekin<=now<=checkout
                        //현재 머무르는 중인 날짜가 존재한다면 true 아니면 false
                        .anyMatch(r -> !r.getCheckInDate().isAfter(now) && !r.getCheckOutDate().isBefore(now));

                if(!hasOngoingReservation){
                    iter.remove();
                    chatParticipantRepository.delete(cp);
                }
            }
        }

        log.info("스케줄러 종료");

    }

}
