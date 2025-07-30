package com.beyond.HanSoom.hotel.service;

import com.beyond.HanSoom.common.S3Uploader;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.hotel.dto.HotelRegisterRequsetDto;
import com.beyond.HanSoom.hotel.dto.HotelStateUpdateDto;
import com.beyond.HanSoom.hotel.dto.HotelUpdateDto;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.room.dto.RoomUpdateDto;
import com.beyond.HanSoom.roomImage.domain.RoomImage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final S3Uploader s3Uploader;
    private final GeocoderService geocoderService;

    public void registerHotel(HotelRegisterRequsetDto dto, MultipartFile hotelImage, List<MultipartFile> roomImages) {
        // 호텔 이미지 S3 저장
        String hotelImageUrl = (hotelImage != null && !hotelImage.isEmpty())
                ? s3Uploader.upload(hotelImage, "hotel")
                : null;
        // 호텔 객체 저장
        GeocoderService.Coordinate coord = geocoderService.getCoordinates(dto.getAddress());
        Hotel hotel = hotelRepository.save(dto.toEntity(hotelImageUrl, coord));

        // 객실 생성
        List<Room> rooms = dto.getRooms().stream().map(a -> a.toEntity(hotel)).toList();

        Map<Integer, List<MultipartFile>> roomImageMap = new HashMap<>();
        for(MultipartFile file : roomImages) {
            int roomIndex = extractRoomIndex(file.getOriginalFilename()); // 0
            roomImageMap.computeIfAbsent(roomIndex - 1, k -> new ArrayList<>()).add(file);
        }

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            List<MultipartFile> imageList = roomImageMap.getOrDefault(i, List.of());

            for (MultipartFile image : imageList) {
                String imageUrl = s3Uploader.upload(image, "room");
                RoomImage roomImage = RoomImage.builder()
                        .imageUrl(imageUrl)
                        .room(room)
                        .build();
                room.getRoomImages().add(roomImage); // 양방향 연관관계 설정
            }
            hotel.getRooms().add(room); // 호텔에 객실 추가
        }
        hotelRepository.save(hotel);
    }

    public void answerAdmin(HotelStateUpdateDto dto) {
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new EntityNotFoundException("등록된 호텔이 없습니다."));
        hotel.updateState(dto.getState());

        for(Room r : hotel.getRooms()) {
            r.updateState(dto.getState());
        }
    }

    private int extractRoomIndex(String filename) {
        String prefix = filename.split("_")[0];     // room1
        return Integer.parseInt(prefix.replace("room", "")); // 0-indexed
    }

    public void updateHotel(Long id, HotelUpdateDto dto, MultipartFile hotelImage, List<MultipartFile> roomImages) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("호텔이 존재하지 않습니다."));
        s3Uploader.delete(hotel.getImage());
        String imageUrl = s3Uploader.upload(hotelImage, "hotel");
        hotel.updateHotel(dto.getHotelName(), dto.getAddress(), dto.getPhoneNumber(), dto.getDescribtion(), dto.getType(), imageUrl);

        Map<Long, Room> existingRooms = hotel.getRooms().stream()
                .collect(Collectors.toMap(Room::getId, r -> r));

        List<Room> updatedRooms = new ArrayList<>();
        Set<Long> updatedIds = new HashSet<>();

        for (RoomUpdateDto roomDto : dto.getRooms()) {
            Room room;

            if (roomDto.getRoomId() != null && existingRooms.containsKey(roomDto.getRoomId())) {
                room = existingRooms.get(roomDto.getRoomId());
                room.updateInfo(roomDto);
                updatedIds.add(room.getId());

                // 기존 이미지 S3에서 삭제
                for (RoomImage img : room.getRoomImages()) {
                    s3Uploader.delete(img.getImageUrl());
                }
                room.getRoomImages().clear();

            } else {
                // 신규 추가
                room = Room.builder()
                        .hotel(hotel)
                        .type(roomDto.getType())
                        .roomCount(roomDto.getRoomCount())
                        .weekPrice(roomDto.getWeekPrice())
                        .weekendPrice(roomDto.getWeekendPrice())
                        .standardPeople(roomDto.getStandardPeople())
                        .maximumPeople(roomDto.getMaximumPeople())
                        .roomOption1(roomDto.getRoomOption1())
                        .roomOption2(roomDto.getRoomOption2())
                        .extraFee(roomDto.getExtraFee())
                        .checkIn(roomDto.getCheckIn())
                        .checkOut(roomDto.getCheckOut())
                        .describtion(roomDto.getDescribtion())
                        .state(HotelState.APPLY)
                        .build();
            }

            List<RoomImage> roomImageList = uploadRoomImagesFor(roomDto.getRoomKey(), roomImages, room);
            room.updateRoomImages(roomImageList);
            updatedRooms.add(room);
        }

        for (Room r : hotel.getRooms()) {
            if (r.getId() != null && !updatedIds.contains(r.getId())) {
                r.updateState(HotelState.REMOVE);

                for (RoomImage img : r.getRoomImages()) {
                    s3Uploader.delete(img.getImageUrl());
                }
                r.getRoomImages().clear();
            }
        }

        hotel.updateRooms(updatedRooms);
    }

    private List<RoomImage> uploadRoomImagesFor(String roomKey, List<MultipartFile> files, Room room) {
        return files.stream()
                .filter(f -> f.getOriginalFilename() != null && f.getOriginalFilename().startsWith(roomKey))
                .map(f -> {
                    String imageUrl = s3Uploader.upload(f, "room");
                    return RoomImage.builder()
                            .room(room)
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();
    }
}
