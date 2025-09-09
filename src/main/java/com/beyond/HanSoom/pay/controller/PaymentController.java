package com.beyond.HanSoom.pay.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.pay.dto.PaymentReqDto;
import com.beyond.HanSoom.pay.dto.PaymentResDto;
import com.beyond.HanSoom.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
        import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentReqDto paymentReqDto) {

        System.out.println(paymentReqDto);
        PaymentResDto paymentResDto = paymentService.pay(paymentReqDto);
        System.out.println("paymentResDto.isSuccess()"+paymentResDto.isSuccess());
        if(paymentResDto.isSuccess()){
            return new ResponseEntity<>(new CommonSuccessDto(paymentResDto.getResponse(), HttpStatus.OK.value(),"payment is success"), HttpStatus.OK);

        }else{
            System.out.println("결제 실패");
            return new ResponseEntity<>(new CommonSuccessDto(paymentResDto.getResponse(), HttpStatus.BAD_REQUEST.value(),"payment is fail"), HttpStatus.BAD_REQUEST);
        }
    }
}
