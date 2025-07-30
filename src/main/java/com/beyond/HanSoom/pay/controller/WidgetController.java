package com.beyond.HanSoom.pay.controller;

import com.beyond.HanSoom.pay.dto.PaymentReqDto;
import com.beyond.HanSoom.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class WidgetController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentReqDto paymentReqDto) {
        // 동기 방식
        Map<String, Object> result = paymentService.pay(paymentReqDto);
        System.out.println(result);
        return ResponseEntity.ok(result);
    }
}
