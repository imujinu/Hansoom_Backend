package com.beyond.HanSoom.reservation.controller;

import com.beyond.HanSoom.common.CommonSuccessDto;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservation")
@Slf4j
public class ReservationController {
    private ReservationService reservationService;

    //예약
    @PostMapping("/confirm")
    public ResponseEntity<?> reservation(@RequestBody ReservationReqDto dto){
        reservationService.confirm(dto);
        System.out.println(dto);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //예약 조회
    @GetMapping("/find")
    public ResponseEntity<?> find(){
        List<ReservationResDto> resDtos = reservationService.find();
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }


    //예약 취소
    @PatchMapping("/cancel")
    public ResponseEntity<?> cancel(){
        UUID reserveId= reservationService.cancel();
        return new ResponseEntity<>(reserveId, HttpStatus.ACCEPTED);
    }




}
