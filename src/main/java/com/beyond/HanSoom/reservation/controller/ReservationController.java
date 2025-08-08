package com.beyond.HanSoom.reservation.controller;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import com.beyond.HanSoom.reservation.dto.req.ReservationCompleteReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResponse;
import com.beyond.HanSoom.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/reservation")
@Slf4j
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    //예약 신청
    @LimitRequestPerTime(prefix = "1", count = 10, ttlTimeUnit = TimeUnit.SECONDS, ttl = 30)
    @PostMapping("/confirm")
    public ResponseEntity<?> reservation(@RequestBody ReservationReqDto dto){
        ReservationResponse response = reservationService.confirm(dto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //예약 확정
    @PostMapping("/complete")
    public ResponseEntity<?> complete(@RequestBody ReservationCompleteReqDto dto){
        String uuid = reservationService.complete(dto);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }
    //예약 전체 조회
    @GetMapping("/find")
    public ResponseEntity<?> find(){
        List<ReservationResDto> resDtos = reservationService.find();
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }


    //예약 취소
    @PatchMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancel(@PathVariable Long reservationId){
        String reserveId= reservationService.cancel(reservationId);
        return new ResponseEntity<>(reserveId, HttpStatus.ACCEPTED);
    }
}
