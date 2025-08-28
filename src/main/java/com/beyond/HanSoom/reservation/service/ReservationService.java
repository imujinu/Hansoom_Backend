package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.common.service.ReservationCacheService;
import com.beyond.HanSoom.notification.service.NotificationService;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.dto.res.ReservationCacheResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@Slf4j
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ReservationCacheService reservationCacheService;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final ChatRoomRepository chatRoomRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, ReservationCacheService reservationCacheService, ReviewRepository reviewRepository, NotificationService notificationService, ChatRoomRepository chatRoomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.reservationCacheService = reservationCacheService;
        this.reviewRepository = reviewRepository;
        this.notificationService = notificationService;
        this.chatRoomRepository = chatRoomRepository;
    }

    public List<ReservationResDto> findAll() {
        User user = getUser();

        List<Reservation> reservation = reservationRepository.findAllByUser(user);
        List<ReservationResDto> reservationList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for(Reservation r : reservation){
//            BigDecimal hotelRating = reviewRepository.findByHotel(r.getHotel()).getRating(); // todo : 리뷰없어서 에러 뜸
            String status = getStatus(r, now);
            Long chatRoomId = chatRoomRepository.findByReservationAndIsGroupChat(r, "N").getId();
            reservationList.add(new ReservationResDto().fromEntity(r, BigDecimal.valueOf(4.5), status, chatRoomId));
        }
        return reservationList;
    }

    private static String getStatus(Reservation r, LocalDate now) {
        String status = "";
        if(r.getCheckOutDate().isAfter(now)){
            status = "upcoming";
        }else{
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
                List<Review> reviewList = reviewRepository.findAllByHotel(reservation.getHotel());
                LocalDate now = LocalDate.now();
                String state = getStatus(reservation,now);
                Long chatRoomId = chatRoomRepository.findByReservationAndIsGroupChat(reservation,"N").getId();
                ReservationCacheResDto cacheResDto = new ReservationCacheResDto().fromEntity(reservation,state  ,reviewList, chatRoomId);
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
