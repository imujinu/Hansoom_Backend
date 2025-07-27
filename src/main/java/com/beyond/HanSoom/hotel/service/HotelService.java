package com.beyond.HanSoom.hotel.service;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.dto.HotelRegisterRequsetDto;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelService {
    public final HotelRepository hotelRepository;

    public void registerHotel(HotelRegisterRequsetDto dto) {
        Hotel hotel = hotelRepository.save(dto.toEntity());
        List<Room> rooms = dto.getRooms().stream().map(a -> a.toEntity(hotel)).toList();
        for(Room r : rooms) {
            hotel.getRooms().add(r);
        }
    }
}
