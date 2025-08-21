package com.beyond.HanSoom.reservation.controller;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.dto.req.ReservationCompleteReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationFindReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationCacheResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResponse;
import com.beyond.HanSoom.reservation.service.ReservationPaymentService;
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
    private final ReservationPaymentService reservationPaymentService;
    //예약 신청
    @LimitRequestPerTime(prefix = "1", count = 10, ttlTimeUnit = TimeUnit.SECONDS, ttl = 30)
    @PostMapping("/confirm")
    public ResponseEntity<?> reservation(@RequestBody ReservationReqDto dto){
        ReservationResponse result = reservationPaymentService.confirm(dto);
        System.out.println("response :::::::: " + result);
        return new ResponseEntity<>(new CommonSuccessDto(result, HttpStatus.ACCEPTED.value(), "결제 요청 중 "), HttpStatus.ACCEPTED);
    }

    //예약 확정
    @PostMapping("/complete")
    public ResponseEntity<?> complete(@RequestBody ReservationCompleteReqDto dto){
        Long reservationId = reservationPaymentService.complete(dto);
        return new ResponseEntity<>(new CommonSuccessDto(reservationId, HttpStatus.ACCEPTED.value(), "예약에 성공하였습니다."), HttpStatus.ACCEPTED);
    }
    //예약 전체 조회
    @GetMapping("/findAll")
    public ResponseEntity<?> findAll(){
        List<ReservationResDto> resDtos = reservationService.findAll();
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }

    @PostMapping("/find")
    public ResponseEntity<?> find(@RequestBody ReservationFindReqDto dto){
        ReservationCacheResDto reservation = reservationService.find(dto.getReservationId());

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }


    //예약 취소
    @PatchMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancel(@PathVariable Long reservationId){
        String reserveId= reservationService.cancel(reservationId);
        return new ResponseEntity<>(reserveId, HttpStatus.ACCEPTED);
    }
}
