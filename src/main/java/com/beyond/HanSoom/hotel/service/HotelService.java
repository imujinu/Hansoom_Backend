package com.beyond.HanSoom.hotel.service;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.dto.HotelRegisterRequsetDto;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.roomImage.domain.RoomImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelService {
    public final HotelRepository hotelRepository;

    public void registerHotel(HotelRegisterRequsetDto dto, MultipartFile hotelImage, List<MultipartFile> roomImages) {
        Hotel hotel = hotelRepository.save(dto.toEntity(hotelImage));
        List<Room> rooms = dto.getRooms().stream().map(a -> a.toEntity(hotel)).toList();
        for(Room r : rooms) {
            hotel.getRooms().add(r);
        }

        Map<Integer, List<MultipartFile>> roomImageMap = new HashMap<>();
        for(MultipartFile file : roomImages) {
            int roomIndex = extractRoomIndex(file.getOriginalFilename()); // 0
            roomImageMap.computeIfAbsent(roomIndex - 1, k -> new ArrayList<>()).add(file);
        }

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            List<MultipartFile> imageList = roomImageMap.getOrDefault(i, List.of());

            for (MultipartFile image : imageList) {
//                String savedFileName = saveImageLocally(image, "uploads/room/");
                RoomImage roomImage = RoomImage.builder()
                        .imageUrl(image.getOriginalFilename())
                        .room(room)
                        .build();
                room.getRoomImages().add(roomImage); // 양방향 연관관계 설정
            }

            hotel.getRooms().add(room); // 호텔에 객실 추가
        }

    }

    private int extractRoomIndex(String filename) {
        String prefix = filename.split("_")[0];     // room1
        return Integer.parseInt(prefix.replace("room", "")); // 0-indexed
    }
}
