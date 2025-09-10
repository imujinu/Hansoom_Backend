package com.beyond.HanSoom.reservation.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.dto.req.QueueReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationCompleteReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationFindReqDto;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationCacheResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationCompleteResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResponse;
import com.beyond.HanSoom.reservation.service.QueueService;
import com.beyond.HanSoom.reservation.service.ReservationPaymentService;
import com.beyond.HanSoom.reservation.service.ReservationService;
import com.beyond.HanSoom.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/reservation")
@Slf4j
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationPaymentService reservationPaymentService;
    private final QueueService queueService;

    //예약 신청
    @PostMapping("/confirm")
    public ResponseEntity<?> reservation(@RequestBody ReservationReqDto dto){
        ReservationResponse result = reservationPaymentService.confirm(dto);
        System.out.println("response :::::::: " + result);
        return new ResponseEntity<>(new CommonSuccessDto(result, HttpStatus.ACCEPTED.value(), "결제 confirm "), HttpStatus.ACCEPTED);
    }

    //예약 확정
    @PostMapping("/complete")
    public ResponseEntity<?> complete(@RequestBody ReservationCompleteReqDto dto){
        ReservationCompleteResDto dtos = reservationPaymentService.complete(dto);
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.ACCEPTED.value(), "예약에 성공하였습니다."), HttpStatus.ACCEPTED);
    }
    //예약 전체 조회
    @GetMapping("/findAll")
    public ResponseEntity<?> findAll(@PageableDefault(value = 4, sort = "id", direction = Sort.Direction.DESC)Pageable pageable){
        Page<ReservationResDto> resDtos = reservationService.findAll(pageable);
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }
    @GetMapping("/host/findAll")
    public ResponseEntity<?> findHostAll(@PageableDefault(value = 4, sort = "id", direction = Sort.Direction.DESC)Pageable pageable){
        Page<ReservationResDto> resDtos = reservationService.hostFindAll(pageable);
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }
    @PostMapping("/find/{reservationId}")
    public ResponseEntity<?> find(@PathVariable Long reservationId){
        ReservationCacheResDto reservation = reservationService.find(reservationId);

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }



    //예약 취소
    @PatchMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancel(@PathVariable Long reservationId){
        String reserveId= reservationPaymentService.cancel(reservationId);
        return new ResponseEntity<>(new CommonSuccessDto(reserveId, HttpStatus.OK.value(), "삭제 완료"), HttpStatus.OK);
    }

    @PostMapping("/enter")
    public ResponseEntity<?> enterQueue(@RequestBody QueueReqDto dto) {
        boolean entered = queueService.enterQueue(dto);
        Map<String, Object> res = new HashMap<>();
        res.put("entered", entered);
        return new ResponseEntity<>(new CommonSuccessDto(res, HttpStatus.OK.value(),"대기열 등록 완료" ), HttpStatus.OK);
    }

    /** 예약 완료 / 취소 시 대기열 제거 */
    @PostMapping("/leave")
    public ResponseEntity<?> leaveQueue(@RequestBody QueueReqDto dto) {
        queueService.leaveQueue(dto);
        return new ResponseEntity<>(new CommonSuccessDto(null, HttpStatus.ACCEPTED.value(), "대기열 제거 성공"), HttpStatus.ACCEPTED);
    }

    /** SSE 연결: 실시간 순위 브로드캐스트 */
    @GetMapping("/connect")
    public SseEmitter subscribeQueue( @RequestParam String hotelId,
                                             @RequestParam String roomId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                                      @RequestParam String userId
    ) {
        System.out.println("connect 요청 시작 =========");
        QueueReqDto dto = new QueueReqDto().makeDto(hotelId, roomId, checkInDate,userId);
        queueService.enterQueue(dto);
        SseEmitter sseEmitter =  queueService.registerEmitter(dto);
        try {
            sseEmitter.send(SseEmitter.event().name("book").data("예약 대기열 연결완료"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sseEmitter;
    }




}
