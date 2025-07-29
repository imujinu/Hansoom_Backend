package com.beyond.HanSoom.reservation.controller;

import com.beyond.HanSoom.common.CommonSuccessDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserve")
public class ReservationController {
    private ReservationService reservationService;

    //예약
    @PostMapping("/reservation")
    public ResponseEntity<?> reservation(@RequestBody ReservationReqDto dto){
       Long reserveId = reservationService.reserve(dto);

        return new ResponseEntity<>("ok", HttpStatus.ACCEPTED); //todo : 빌더패턴으로 리턴
    }

    //예약 조회
    @GetMapping("/find")
    public ResponseEntity<?> find(){
        return null;
    }


    //예약 취소
    @PatchMapping("/cancel")
    public ResponseEntity<?> cancel(){
        return null;
    }

    //결제
    @RequestMapping("/pay")
    public ResponseEntity<?> pay(){
        return null;
    }

}
