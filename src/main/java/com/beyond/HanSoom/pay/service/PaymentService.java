package com.beyond.HanSoom.pay.service;

import com.beyond.HanSoom.pay.dto.PaymentReqDto;
import com.beyond.HanSoom.pay.repository.PaymentRepository;
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
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.tosspayments.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public Map<String, Object> pay(PaymentReqDto paymentReqDto) {
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
        return response;

            /*
            *
            *
            * */


    }
}
