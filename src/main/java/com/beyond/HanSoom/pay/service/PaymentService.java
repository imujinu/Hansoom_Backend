package com.beyond.HanSoom.pay.service;

import com.beyond.HanSoom.pay.domain.Payment;
import com.beyond.HanSoom.pay.dto.PaymentReqDto;
import com.beyond.HanSoom.pay.dto.PaymentResDto;
import com.beyond.HanSoom.pay.repository.PaymentRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.tosspayments.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

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
        Reservation reservation =  reservationRepository.findById(Long.parseLong(paymentReqDto.getOrderId())).orElseThrow(()->new EntityNotFoundException("존재하지 않는 주문입니다."));



        if ("DONE".equals(response.get("status")) && reservation.getState()==State.PENDING) {
            reservation.changeState(State.SUCCESS);
            paymentRepository.save(Payment.builder()
                            .reservation(reservation)
                            .paymentType(response.get("method").toString())
                            .price(response.get("totalAmount").toString())
                            .state(State.SUCCESS)
                    .build());
            return PaymentResDto.builder().response(response).isSuccess(true).build();
        } else {
            reservation.changeState(State.FAIL);
            paymentRepository.save(Payment.builder()
                    .reservation(reservation)
                    .paymentType(response.get("method").toString())
                    .price(response.get("totalAmount").toString())
                    .state(State.FAIL)
                    .build());
        }
        return PaymentResDto.builder().response(null).build();
    }
}
