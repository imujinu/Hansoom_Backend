package com.beyond.HanSoom.hotel.controller;

import com.beyond.HanSoom.common.CommonSuccessDto;
import com.beyond.HanSoom.hotel.dto.HotelRegisterRequsetDto;
import com.beyond.HanSoom.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotel")
public class HotelController {

    private final HotelService hotelService;

    @PostMapping("/create")
    public ResponseEntity<?> registerHotel(@RequestBody HotelRegisterRequsetDto dto) {
        hotelService.registerHotel(dto);
        return new ResponseEntity<>(new CommonSuccessDto("OK", HttpStatus.CREATED.value(), "hotel is created"), HttpStatus.CREATED);
    }

}
