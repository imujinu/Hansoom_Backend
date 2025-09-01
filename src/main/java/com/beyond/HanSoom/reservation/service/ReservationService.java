package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.common.service.ReservationCacheService;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.notification.service.NotificationService;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.dto.res.ReservationCacheResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.repository.HotelReviewSummaryRepository;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserRole;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ReservationCacheService reservationCacheService;
    private final ReviewRepository reviewRepository;
    private final HotelReviewSummaryRepository hotelReviewSummaryRepository;
    private final NotificationService notificationService;
    private final ChatRoomRepository chatRoomRepository;
    private final HotelRepository hotelRepository;



    public Page<ReservationResDto> findAll(Pageable pageable) {
        String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority().split("_")[1];
        if(role.equals(UserRole.USER) || role.equals(UserRole.ADMIN)){

        User user = getUser();
        LocalDate now = LocalDate.now();


        // 1️⃣ 유저 예약 전체 조회
        List<Reservation> allReservations = reservationRepository.findAllByUser(user);

        // 2️⃣ 상태 필터 후 DTO 변환
        List<ReservationResDto> allDtos = allReservations.stream()
                .map(r -> {
                    Hotel hotel = r.getHotel();
                    String status = getStatus(r,now);
                    List<ChatRoom> chatRooms = chatRoomRepository.findAllByHotelAndIsGroupChat(hotel,"N");
                    ChatRoom chatRoom = null;
                    for (ChatRoom cr : chatRooms) {
                        for (ChatParticipant cp : cr.getParticipantList()) {
                            if (cp.getUser().equals(user)) {
                                chatRoom = cr;
                                break;
                            }
                        }
                    }
                    BigDecimal hotelRating = hotelReviewSummaryRepository.findByHotel(hotel).getRatingSum();
                    Long chatRoomId = chatRoom != null ? chatRoom.getId() : null;

                    return new ReservationResDto().fromEntity(r, hotelRating, status, chatRoomId);
                })
                .collect(Collectors.toList());

        // 3️⃣ Pageable 적용
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allDtos.size());

        List<ReservationResDto> pageList;
        if (start > allDtos.size()) {
            pageList = Collections.emptyList();
        } else {
            pageList = allDtos.subList(start, end);
        }

        return new PageImpl<>(pageList, pageable, allDtos.size());
        }


        else{
            User user = getUser();
            LocalDate now = LocalDate.now();
            Hotel hostHotel = hotelRepository.findByUser(user);

            // 1️⃣ 유저 예약 전체 조회
            List<Reservation> allReservations = reservationRepository.findAllByHotel(hostHotel);

            // 2️⃣ 상태 필터 후 DTO 변환
            List<ReservationResDto> allDtos = allReservations.stream()
                    .map(r -> {
                        Hotel hotel = r.getHotel();
                        String status = getStatus(r,now);
                        List<ChatRoom> chatRooms = chatRoomRepository.findAllByHotelAndIsGroupChat(hotel,"N");
                        ChatRoom chatRoom = null;
                        for (ChatRoom cr : chatRooms) {
                            for (ChatParticipant cp : cr.getParticipantList()) {
                                if (cp.getUser().equals(user)) {
                                    chatRoom = cr;
                                    break;
                                }
                            }
                        }
                        BigDecimal hotelRating = hotelReviewSummaryRepository.findByHotel(hotel).getRatingSum();
                        Long chatRoomId = chatRoom != null ? chatRoom.getId() : null;

                        return new ReservationResDto().fromEntity(r, hotelRating, status, chatRoomId);
                    })
                    .collect(Collectors.toList());

            // 3️⃣ Pageable 적용
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allDtos.size());

            List<ReservationResDto> pageList;
            if (start > allDtos.size()) {
                pageList = Collections.emptyList();
            } else {
                pageList = allDtos.subList(start, end);
            }

            return new PageImpl<>(pageList, pageable, allDtos.size());
        }
    }


    private static String getStatus(Reservation r, LocalDate now) {
        String status = "";
        if(now.isBefore(r.getCheckInDate())){
            status = "upcoming";
        }else if ((now.isEqual(r.getCheckInDate()) || now.isAfter(r.getCheckInDate()))
                && now.isBefore(r.getCheckOutDate())) {
            status = "ongoing";
        }else if (now.isAfter(r.getCheckOutDate())) {
            status = "completed";
        }

        if(r.getState() == State.CANCELLED){
            status = "canceled";
        }
        return status;
    }

    public ReservationCacheResDto find(Long id) {
        try {
            //유저 검증 로직
            ReservationCacheResDto cacheReservation = reservationCacheService.getCacheReservation(id);

            if(cacheReservation == null){
                Reservation reservation = reservationRepository.findById(id).orElseThrow(()->new EntityNotFoundException("예약 내역이 존재하지 않습니다."));
                Hotel hotel = reservation.getHotel();
                List<ChatRoom> chatRooms = chatRoomRepository.findAllByHotelAndIsGroupChat(hotel,"N");
                ChatRoom chatRoom = null;
                User user = getUser();
                for(ChatRoom cr :chatRooms){
                    for(ChatParticipant cp : cr.getParticipantList()){
                        if(cp.getUser().equals(user)){
                            chatRoom = cr;
                            break;
                        }
                    }
                }

                if(chatRoom==null){
                    throw new EntityNotFoundException("채팅방이 존재하지 않습니다.");
                }

                List<Review> reviewList = reviewRepository.findAllByHotel(reservation.getHotel());
                LocalDate now = LocalDate.now();
                String state = getStatus(reservation,now);
                ReservationCacheResDto cacheResDto = new ReservationCacheResDto().fromEntity(reservation,state  ,reviewList, chatRoom.getId());
                if(reservation.getState() == State.RESERVED){
                reservationCacheService.saveCacheReservation(cacheResDto);
                return cacheResDto;
                }else{
                    throw new IllegalStateException("유효하지 않은 예약입니다.");
                }
            }else{
                return cacheReservation;
            }
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            throw new EntityNotFoundException("해당 예약이 존재하지 않습니다.");
        }
    }

    public User getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        return user;
    }

}
