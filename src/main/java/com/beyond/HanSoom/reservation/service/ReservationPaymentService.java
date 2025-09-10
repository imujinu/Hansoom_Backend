package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.common.dto.QueueReservationReqDto;
import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.common.service.QueueReservationService;
import com.beyond.HanSoom.common.service.ReservationInventoryService;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.notification.repository.NotificationRepository;
import com.beyond.HanSoom.notification.service.NotificationService;
import com.beyond.HanSoom.notification.service.SseAlarmService;
import com.beyond.HanSoom.pay.domain.Payment;
import com.beyond.HanSoom.pay.repository.PaymentRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.dto.req.ReservationCompleteReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationCompleteResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResponse;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.room.repository.RoomRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.beyond.HanSoom.pay.service.PaymentService.generateQueueKey;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Service
@Transactional
@Slf4j
public class ReservationPaymentService {
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReservationInventoryService reservationInventoryService;
    private final QueueReservationService queueReservationService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SseAlarmService sseAlarmService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    public ReservationPaymentService(UserRepository userRepository, HotelRepository hotelRepository, RoomRepository roomRepository, ReservationInventoryService reservationInventoryService, QueueReservationService queueReservationService, @Qualifier("reservationList")RedisTemplate<String, String> redisTemplate, ReservationRepository reservationRepository, PaymentRepository paymentRepository, SseAlarmService sseAlarmService, NotificationRepository notificationRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.reservationInventoryService = reservationInventoryService;
        this.queueReservationService = queueReservationService;
        this.redisTemplate = redisTemplate;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.sseAlarmService = sseAlarmService;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    public ReservationResponse confirm(ReservationReqDto dto) {

        try{
            // 값 유효성 검증
            User user = getUser();
            Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(()->new EntityNotFoundException("해당 호텔이 존재하지 않습니다."));;
            Room room = roomRepository.findByIdAndHotel(dto.getRoomId(),hotel).orElseThrow(()-> new EntityNotFoundException("해당 객실이 존재하지 않습니다."));

            if(user.equals(hotel.getUser())){
                throw new IllegalArgumentException("본인의 호텔은 예약이 불가능합니다.");
            }

            // 예약 인원 검증
            if(dto.getPeople()>room.getMaximumPeople()){
                throw new IllegalArgumentException("인원이 초과 되었습니다.");
            }

            // 실제 숙박비 계산
            long totalPrice = getTotalPrice(dto, room);

            ReservationDto reservationDto = new ReservationDto().makeDto(hotel, room,user, dto.getCheckIn(), dto.getCheckOut(), room.getRoomCount());
//        reservationInventoryService.increaseInventory(reservationDto);

            System.out.println("inventory" + reservationInventoryService.getInventory(reservationDto));
            Reservation reservation = dto.toEntity(totalPrice,user,hotel, room);
            ReservationResponse response = makeReservation(reservationDto, reservation);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public ReservationResponse makeReservation(ReservationDto request, Reservation reservation) {

        List<Long> queueResult = queueReservationService.addToQueue(new QueueReservationReqDto().makeDto(request));

        long result = queueResult.get(0);
        List<String> keys = new ArrayList<>();
        generateQueueKey(reservation, reservation.getCheckInDate(), reservation.getCheckOutDate(), keys);
        
        if (result == -2) {

            return ReservationResponse.fail(reservation, "재고가 부족합니다.");
        } else if (result == -1) {
            return ReservationResponse.fail(reservation, "기존 예약 내역이 존재합니다.");
        }
        else  {
                Reservation pendingReservation = reservationRepository.save(reservation);
                return ReservationResponse.success(pendingReservation);
        }
    }

    private void deleteRedisReservation(Reservation reservation, List<String> keys) {
        for(int i = 0; i< keys.size(); i++){

        queueReservationService.removeMember(keys.get(i), String.valueOf(reservation.getUser().getId()) );
        }
    }

    public ReservationCompleteResDto complete(ReservationCompleteReqDto dto) {
        Reservation reservation = reservationRepository.findByUuid(dto.getReservationId()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 예약 내역 입니다."));
        Payment payment = paymentRepository.findByReservationId(reservation.getId());
        User user = getUser();
        List<String> keys = new ArrayList<>();
        generateQueueKey(reservation, reservation.getCheckInDate(), reservation.getCheckOutDate(), keys);

        if(reservation.getState() == State.SUCCEED && payment.getReservation().getState() == State.SUCCEED){
            LocalDate ttlDate = reservation.getCheckOutDate();
            for(int i=0; i<keys.size(); i++){
                queueReservationService.updateStatus(keys.get(i), String.valueOf(user.getId()), "RESERVED");
                queueReservationService.setTtl(keys.get(i),  ttlDate);
            }
            reservation.changeState(State.RESERVED);
            reservation.getHotel().updateCount();
            // 사용자, 호스트 알림 저장 (전송은 호스트만)
            notificationService.createNotiNewBookingForHost(user, reservation);
            notificationService.createNotiBookingConfirmed(user, reservation);
            notificationService.createNotiStayReminderD1(user, reservation);
            notificationService.createNotiReviewRequest(user, reservation);
            sseAlarmService.publishReserved(reservation.getHotel().getUser().getEmail(), "reserved");

            return new ReservationCompleteResDto().fromEntity(reservation.getId());
        }else{
            for(int i=0; i<keys.size(); i++){
                queueReservationService.removeMember(keys.get(i), String.valueOf(user.getId()));
            }
            reservation.changeState(State.VALIDATION_FAILED);
            throw new IllegalStateException("결제가 완료되지 않은 주문 입니다.");
        }
    }
    public String cancel(Long reservationId){
        User user = getUser();
        Reservation reservation = reservationRepository.findByIdAndUser(reservationId,user);
        reservation.changeState(State.CANCELLED);
        List<String> keys = new ArrayList<>();
        generateQueueKey(reservation,reservation.getCheckInDate(),reservation.getCheckOutDate(), keys);
        for(int i=0; i<keys.size(); i++){
            queueReservationService.removeMember(keys.get(i), String.valueOf(user.getId()));
        }
        notificationService.cancelAllNotificationsByReservation(reservationId);
        return reservation.getUuid();
    }

    private static long getTotalPrice(ReservationReqDto dto, Room room) {
        LocalDate date = dto.getCheckIn();

        long totalPrice = 0;

        while(!date.isEqual(dto.getCheckOut())){
            DayOfWeek day = date.getDayOfWeek();
            if(day==SATURDAY || day==SUNDAY ){
                totalPrice += room.getWeekendPrice();
            }else{
                totalPrice += room.getWeekPrice();
            }
            date=date.plusDays(1);
        }
        return totalPrice;
    }

    public User getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        return user;
    }

}
