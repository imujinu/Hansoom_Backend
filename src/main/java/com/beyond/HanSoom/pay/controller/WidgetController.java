package com.beyond.HanSoom.pay.controller;

import com.beyond.HanSoom.common.CommonSuccessDto;
import com.beyond.HanSoom.pay.dto.PaymentReqDto;
import com.beyond.HanSoom.pay.dto.PaymentResDto;
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

        PaymentResDto paymentResDto = paymentService.pay(paymentReqDto);
        if(paymentResDto.isSuccess()){
        return new ResponseEntity<>(new CommonSuccessDto(paymentResDto.getResponse(), HttpStatus.OK.value(),"payment is success"), HttpStatus.OK);

        }else{
            return new ResponseEntity<>(new CommonSuccessDto(paymentResDto.getResponse(), HttpStatus.BAD_REQUEST.value(),"payment is fail"), HttpStatus.BAD_REQUEST);
        }
    }
}
