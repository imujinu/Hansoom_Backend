package com.beyond.HanSoom.hotel.repository;

import com.beyond.HanSoom.hotel.dto.HotelCursorRequestDto;
import com.beyond.HanSoom.hotel.dto.HotelListResponseDto;
import java.util.List;

public interface HotelCustomRepository {
    List<HotelListResponseDto> searchByCursor(HotelCursorRequestDto requestDto);
}
