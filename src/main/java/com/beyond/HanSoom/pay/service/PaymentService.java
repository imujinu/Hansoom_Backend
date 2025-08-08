package com.beyond.HanSoom.pay.service;

import com.beyond.HanSoom.common.dto.QueueReservationReqDto;
import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.common.service.QueueReservationService;
import com.beyond.HanSoom.common.service.RedisDistributedLock;
import com.beyond.HanSoom.pay.domain.Payment;
import com.beyond.HanSoom.pay.dto.PaymentReqDto;
import com.beyond.HanSoom.pay.dto.PaymentResDto;
import com.beyond.HanSoom.pay.repository.PaymentRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RedisDistributedLock distributedLock; // 락
    private final RedisTemplate<String, String> redisTemplate;
    private final QueueReservationService queueReservationService;


    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.tosspayments.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository, RedisDistributedLock distributedLock, @Qualifier("reservationList")RedisTemplate<String, String> redisTemplate, QueueReservationService queueReservationService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.distributedLock = distributedLock;
        this.redisTemplate = redisTemplate;
        this.queueReservationService = queueReservationService;
    }

    public PaymentResDto pay(PaymentReqDto paymentReqDto) {
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        String basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        Map<String, String> requestBody = Map.of(
                "orderId", paymentReqDto.getOrderId(),
                "amount", paymentReqDto.getAmount(),
                "paymentKey", paymentReqDto.getPaymentKey()
        );

        Map<String, Object> response = webClient.post()
                .uri("/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, basicAuth)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Reservation reservation = reservationRepository.findByUuid(paymentReqDto.getOrderId());
        User user = reservation.getUser();
        LocalDate start = reservation.getCheckInDate();
        LocalDate end = reservation.getCheckOutDate();
        List<String> keys = new ArrayList<>();
        generateQueueKey(reservation, start,end, keys);

        // 1. Redis에서 모든 날짜 상태 조회 및 PROCESSING인지 확인
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            String statusKey = String.format("status:hotel:%s:room:%s:date:%s",
                    reservation.getHotel().getId(),
                    reservation.getRoom().getId(),
                    date);

            String redisStatus = (String) redisTemplate.opsForHash().get(statusKey, user.getId() );
            if (!"PROCESSING".equals(redisStatus)) {
                return PaymentResDto.builder()
                        .response(response)
                        .isSuccess(false)
                        .message("예약 상태가 PROCESSING이 아닙니다.")
                        .build();
            }
        }

        boolean isSuccess = "DONE".equals(response.get("status"));

        if (isSuccess) {
            if (reservation.getState() == State.PENDING) {
                reservation.changeState(State.SUCCESS);

                paymentRepository.save(Payment.builder()
                        .reservation(reservation)
                        .paymentType(response.get("method").toString())
                        .price(response.get("totalAmount").toString())
                        .state(State.SUCCESS)
                        .build());

                // 상태 CONFIRMED로 변경

                for(int i=0; i<keys.size(); i++){

                queueReservationService.updateStatus(keys.get(i), user.getId().toString(), "PROCESSING",  "CONFIRMED");
                }
            }
        } else {
            // 결제 실패시 상태 FAIL로 변경

            for(int i=0; i<keys.size(); i++){

                queueReservationService.updateStatus(keys.get(i), user.getId().toString(), "PROCESSING",  "FAIL");
            }
        }

        List<String> lockKey = generateLockKey(reservation);
        String lockValue = generateLockValue(user.getId());

        distributedLock.releaseLock(lockKey, lockValue);
        queueReservationService.processNextInQueue(lockKey + ":queue", lockKey, lockValue, 30000);

        // 5. 결과 반환
        return PaymentResDto.builder()
                .response(isSuccess ? response : null)
                .isSuccess(isSuccess)
                .message(isSuccess ? "결제 성공" : "결제 실패")
                .build();
    }


    public static void generateQueueKey(Reservation reservation, LocalDate start, LocalDate end, List<String> keys) {
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            keys.add(String.format(
                    "queue:hotel:%s:room:%s:date:%s",
                    reservation.getHotel().getId(),
                    reservation.getRoom().getId(),
                    date
            ));
        }
    }

    private List<String> generateLockKey(Reservation reservation) {
        List<String> lockKeys = new ArrayList<>();
        for(LocalDate date = reservation.getCheckInDate(); date.isBefore(reservation.getCheckOutDate()); date= date.plusDays(1)){

        String lockKey = String.format("lock:queue:hotel:%s:room:%s:date:%s",
                reservation.getHotel().getId(),
                reservation.getRoom().getId(),
                date);
        lockKeys.add(lockKey);
        }
        return lockKeys;
    }

    private String generateLockValue(Long userId) {
        return "user:" + userId;
    }

}
