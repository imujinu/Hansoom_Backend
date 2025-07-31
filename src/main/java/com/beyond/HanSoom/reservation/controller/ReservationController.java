package com.beyond.HanSoom.reservation.controller;

import com.beyond.HanSoom.common.CommonSuccessDto;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.dto.req.ReservationCompleResDto;
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

    //예약 신청
    @PostMapping("/confirm")
    public ResponseEntity<?> reservation(@RequestBody ReservationReqDto dto){
        String uuId = reservationService.confirm(dto);

        return new ResponseEntity<>(uuId, HttpStatus.OK);
    }

    //예약 확정
    @PostMapping
    public ResponseEntity<?> complete(@RequestBody ReservationCompleResDto resDto){
        String uuid = reservationService.complete(resDto.getOrderId());
        return new ResponseEntity<>(uuid, HttpStatus.OK);
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
        String reserveId= reservationService.cancel();
        return new ResponseEntity<>(reserveId, HttpStatus.ACCEPTED);
    }




}
