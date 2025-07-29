package com.beyond.HanSoom.hotel.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.hotel.dto.HotelRegisterRequsetDto;
import com.beyond.HanSoom.hotel.dto.HotelStateUpdateDto;
import com.beyond.HanSoom.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotel")
public class HotelController {

    private final HotelService hotelService;

    @PostMapping("/create")
    public ResponseEntity<?> registerHotel(@RequestPart(name = "hotelRegisterDto") HotelRegisterRequsetDto dto,
                                           @RequestPart(name = "hotelImage") MultipartFile hotelImage,
                                           @RequestPart(name = "roomImages") List<MultipartFile> roomImages) {
        hotelService.registerHotel(dto, hotelImage, roomImages);
        return new ResponseEntity<>(new CommonSuccessDto("OK", HttpStatus.CREATED.value(), "hotel is created"), HttpStatus.CREATED);
    }

    @PostMapping("/answerAdmin") // todo Admin만 가능하게 추후 수정
    public ResponseEntity<?> answerAdmin(@RequestBody HotelStateUpdateDto dto) {
        hotelService.answerAdmin(dto);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 등록 답변 완료")
                        .build(),
                HttpStatus.OK
        );
    }

}
