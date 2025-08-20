package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.common.dto.QueueReservationReqDto;
import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.common.service.QueueReservationService;
import com.beyond.HanSoom.common.service.RedisDistributedLock;
import com.beyond.HanSoom.common.service.ReservationCacheService;
import com.beyond.HanSoom.common.service.ReservationInventoryService;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.notification.domain.NotificationState;
import com.beyond.HanSoom.notification.repository.NotificationRepository;
import com.beyond.HanSoom.notification.service.NotificationService;
import com.beyond.HanSoom.notification.service.SseAlarmService;
import com.beyond.HanSoom.pay.domain.Payment;
import com.beyond.HanSoom.pay.repository.PaymentRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.dto.req.ReservationCompleteReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationCacheResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResponse;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.room.repository.RoomRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.beyond.HanSoom.pay.service.PaymentService.generateQueueKey;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Service
@Transactional
@Slf4j
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationInventoryService reservationInventoryService;
    private final QueueReservationService queueReservationService;
    private final RedisDistributedLock distributedLock; // 락
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessageSendingOperations messageTemplates;
    private final ReservationCacheService reservationCacheService;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final SseAlarmService sseAlarmService;
    private final NotificationRepository notificationRepository;


    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, RoomRepository roomRepository, HotelRepository hotelRepository,
                              PaymentRepository paymentRepository, ReservationInventoryService reservationInventoryService, QueueReservationService queueReservationService,
                              RedisDistributedLock distributedLock, @Qualifier("reservationList") RedisTemplate<String, String> redisTemplate, SimpMessageSendingOperations messageTemplates, ReservationCacheService reservationCacheService, ReviewRepository reviewRepository, NotificationService notificationService, SseAlarmService sseAlarmService, NotificationRepository notificationRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.paymentRepository = paymentRepository;
        this.reservationInventoryService = reservationInventoryService;
        this.queueReservationService = queueReservationService;
        this.distributedLock = distributedLock;
        this.redisTemplate = redisTemplate;
        this.messageTemplates = messageTemplates;
        this.reservationCacheService = reservationCacheService;
        this.reviewRepository = reviewRepository;
        this.notificationService = notificationService;
        this.sseAlarmService = sseAlarmService;
        this.notificationRepository = notificationRepository;
    }

    public ReservationResponse confirm(ReservationReqDto dto) {
        // 값 유효성 검증
        User user = getUser(); // todo : 테스트를 위해 더미 유저 찾아오기 나중에 수정할 것
//        User user = userRepository.findById(1L).orElseThrow(()->new EntityNotFoundException("유저가 없습니다"));
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(()->new EntityNotFoundException("해당 호텔이 존재하지 않습니다."));;
        Room room = roomRepository.findByIdAndHotel(dto.getRoomId(),hotel).orElseThrow(()-> new EntityNotFoundException("해당 객실이 존재하지 않습니다."));

        // 예약 인원 검증
        if(dto.getPeople()>room.getMaximumPeople()){
            throw new IllegalStateException("인원이 초과 되었습니다.");
        }

//        // 예약 가능 여부 검증
//        List<Reservation> reservationList = reservationRepository.checkRoom(user,room,hotel, dto.getCheckIn(), dto.getCheckOut(), State.RESERVED);
//        if(reservationList.size()>room.getRoomCount()){
//            throw new IllegalArgumentException("빈 객실이 존재하지 않습니다.");
//        }


        // 실제 숙박비 계산
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

        ReservationDto reservationDto = new ReservationDto().makeDto(hotel, room,user, dto.getCheckIn(), dto.getCheckOut(), room.getRoomCount());
//        reservationInventoryService.increaseInventory(reservationDto);

        System.out.println("inventory" + reservationInventoryService.getInventory(reservationDto));
        Reservation reservation = dto.toEntity(totalPrice,user,hotel, room);
        ReservationResponse response = makeReservation(reservationDto, reservation);
        System.out.println(response.getReservationId());
        return response;
    }


    public List<ReservationResDto> findAll() {
        User user = getUser(); //todo : 추후 수정
//        User user = userRepository.findById(1L).orElseThrow(()->new EntityNotFoundException("유저가 없습니다"));

        List<Reservation> reservation = reservationRepository.findAllByUser(user);
        List<ReservationResDto> reservationList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for(Reservation r : reservation){
//            BigDecimal hotelRating = reviewRepository.findByHotel(r.getHotel()).getRating(); // todo : 리뷰없어서 에러 뜸
            String status = getStatus(r, now);


            reservationList.add(new ReservationResDto().fromEntity(r, BigDecimal.valueOf(4.5), status));
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
                ReservationCacheResDto cacheResDto = new ReservationCacheResDto().fromEntity(reservation,state  ,reviewList);
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


    public String cancel(Long reservationId){
        User user = getUser();
        Reservation reservation = reservationRepository.findByIdAndUser(reservationId,user);
        reservation.cancel();
        notificationService.cancelAllNotificationsByReservation(reservationId);
        return reservation.getUuid();
    }

    public Long complete(ReservationCompleteReqDto dto) {
        Reservation reservation = reservationRepository.findByUuid(dto.getReservationId()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 예약 내역 입니다."));
        Payment payment = paymentRepository.findByReservationId(reservation.getId());
        User user = getUser(); //todo : 추후 수정
//        User user = userRepository.findById(1L).orElseThrow(()->new EntityNotFoundException("유저가 없습니다"));
        ReservationDto reservationDto = new ReservationDto().makeDto(reservation.getHotel(), reservation.getRoom(), user,  reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getRoom().getRoomCount());
        List<String> keys = new ArrayList<>();
        generateQueueKey(reservation, reservation.getCheckInDate(), reservation.getCheckOutDate(), keys);
        if(reservation.getState() == State.SUCCEED && payment.getReservation().getId().equals(reservation.getId())){
            for(int i=0; i<keys.size(); i++){
            queueReservationService.updateStatus(keys.get(i), String.valueOf(user.getId()), "SUCCEED", "RESERVED");
            }
            reservation.changeState(State.RESERVED);

            // 사용자, 호스트 알림 저장 (전송은 호스트만)
            notificationService.createNotiNewBookingForHost(user, reservation);
            notificationService.createNotiBookingConfirmed(user, reservation);
            notificationService.createNotiStayReminderD1(user, reservation);
            notificationService.createNotiReviewRequest(user, reservation);
            sseAlarmService.publishReserved(reservation.getHotel().getUser().getEmail(), "reserved");

            return reservation.getId();
        }else{
            for(int i=0; i<keys.size(); i++){
                queueReservationService.updateStatus(keys.get(i), String.valueOf(user.getId()), "FAILED", "VALIDATION_FAILED");
            }
            reservation.changeState(State.VALIDATION_FAILED);
            throw new IllegalStateException("결제가 완료되지 않은 주문 입니다.");
        }
    }

    public User getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        return user;
    }

    public ReservationResponse makeReservation(ReservationDto request, Reservation reservation) {
        // 1. 대기열 등록 시도
        List<Long> queueResult = queueReservationService.addToQueue(new QueueReservationReqDto().makeDto(request));
        User user = getUser(); // todo : 추후 수정
//        User user = userRepository.findById(1L).orElseThrow(()->new EntityNotFoundException("유저가 없습니다"));
        long position = queueResult.get(0);
        System.out.println(position);
        if (position == -2) {
            return ReservationResponse.fail("재고가 없습니다.");
        } else if (position == -1) {
            return ReservationResponse.fail("이미 대기열에 등록되어 있습니다.");
        }
        if (position == 1) {
            LocalDate start = request.getCheckIn();
            LocalDate end = request.getCheckOut();
            // 바로 예약 처리 시도 (대기열 맨 앞)
            List<String> lockKey = generateLockKey(request);
            String lockValue = generateLockValue(user.getId());
            if (distributedLock.tryLock(lockKey, lockValue, 30000)) {
                    // 예약 실제 처리 (DB 등)
                    for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                        String statusKey = String.format("queue:hotel:%s:room:%s:date:%s",
                                request.getHotelId(),
                                request.getRoomId(),
                                date
                        );
                        System.out.println("여기서 터짐:1 ");
                        String queueKey = "queue:hotel:" + request.getHotelId() + ":room:" + request.getRoomId() + ":date:" + date;
                        System.out.println("여기서 터짐:2 ");
//                        redisTemplate.opsForHash().put(statusKey, String.valueOf(request.getUserId()), "PROCESSING");
//                        redisTemplate.opsForHash().put(statusKey, String.valueOf(request.getUserId()), "PROCESSING");
                        System.out.println("여기서 터짐 : 3");
                        queueReservationService.processNextInQueue(queueKey, lockKey, lockValue, 1500);
                        System.out.println("여기서 터짐 : 4");
                        redisTemplate.expire(statusKey, 1500, TimeUnit.SECONDS);  // TTL 설정
                    }
                        Reservation pendingReservation = reservationRepository.save(reservation);
                    // 2. 바로 성공 리턴 → 프론트에서 결제 화면으로 이동
                    return ReservationResponse.success(pendingReservation.getUuid());

            } else {
                // 락 못 얻으면 대기열에서 대기
                return ReservationResponse.waiting(position);
            }
        }
        else if( position<10){
            List<LocalDate> dates = new ArrayList<>();
            for(LocalDate date= request.getCheckIn(); date.isBefore(request.getCheckOut()); date= date.plusDays(1)){
                dates.add(date);
            }
            for(int i=0; i<dates.size(); i++){
                String chatRoom = String.format("hotelId:%s:roomId:%s:date:%s", request.getHotelId(),request.getRoomId(),dates.get(i));


            messageTemplates.convertAndSend(chatRoom, position);
            }
            return ReservationResponse.connect("CONNECT");
        }

        else {
            // 대기 중 상태 리턴
            return ReservationResponse.waiting(position);
        }
    }

    private List<String> generateLockKey(ReservationDto request) {
        List<String> keys = new ArrayList<>();
        for(LocalDate date = request.getCheckIn(); date.isBefore(request.getCheckOut()) ; date = date.plusDays(1)){

        String key = String.format("lock:queue:hotel:%s:room:%s:date:%s",
                request.getHotelId(),
                request.getRoomId(),
                date);
        keys.add(key);

        }
        return keys;
    }

    private String generateLockValue(Long userId) {
        return "user:" + userId;
    }


}
