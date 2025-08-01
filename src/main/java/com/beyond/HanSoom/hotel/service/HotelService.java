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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("호텔이 존재하지 않습니다."));

        // 1. 백업용 이미지 URL 저장 (롤백을 위해)
        String oldHotelImageUrl = hotel.getImage();
        List<String> uploadedImageUrls = new ArrayList<>();

        try {
            // 2. 호텔 기본 정보 업데이트 (이미지 제외)
            hotel.updateBasicInfo(dto.getHotelName(), dto.getAddress(),
                    dto.getPhoneNumber(), dto.getDescribtion(), dto.getType());

            // 3. 호텔 이미지 처리
            String newHotelImageUrl = null;
            if (hotelImage != null && !hotelImage.isEmpty()) {
                newHotelImageUrl = s3Uploader.upload(hotelImage, "hotel");
                uploadedImageUrls.add(newHotelImageUrl);
                hotel.updateImage(newHotelImageUrl);
            }

            // 4. 객실 정보 처리
            List<Room> updatedRooms = processRoomUpdates(hotel, dto.getRooms(), roomImages, uploadedImageUrls);
            hotel.updateRooms(updatedRooms);

            // 5. DB 저장 (트랜잭션 커밋)
            hotelRepository.save(hotel);

            // 6. 성공 후 기존 이미지 삭제
            cleanupOldImages(hotel, oldHotelImageUrl);

        } catch (Exception e) {
            // 7. 실패 시 업로드된 이미지들 롤백
            rollbackUploadedImages(uploadedImageUrls);
            throw new RuntimeException("호텔 업데이트 실패: " + e.getMessage(), e);
        }
    }

    private List<Room> processRoomUpdates(Hotel hotel, List<RoomUpdateDto> roomDtos,
                                          List<MultipartFile> roomImages, List<String> uploadedImageUrls) {

        Map<Long, Room> existingRooms = hotel.getRooms().stream()
                .collect(Collectors.toMap(Room::getId, Function.identity()));

        List<Room> updatedRooms = new ArrayList<>();
        Set<Long> processedRoomIds = new HashSet<>();

        for (RoomUpdateDto roomDto : roomDtos) {
            Room room = processIndividualRoom(hotel, roomDto, existingRooms, roomImages, uploadedImageUrls);
            updatedRooms.add(room);

            if (room.getId() != null) {
                processedRoomIds.add(room.getId());
            }
        }

        // 삭제된 객실 처리
        markRemovedRooms(hotel.getRooms(), processedRoomIds);

        return updatedRooms;
    }

    private Room processIndividualRoom(Hotel hotel, RoomUpdateDto roomDto, Map<Long, Room> existingRooms,
                                       List<MultipartFile> roomImages, List<String> uploadedImageUrls) {
        Room room;

        if (roomDto.getRoomId() != null && existingRooms.containsKey(roomDto.getRoomId())) {
            // 기존 객실 업데이트
            room = existingRooms.get(roomDto.getRoomId());
            room.updateInfo(roomDto);

            // 기존 이미지는 성공 후 삭제하도록 마킹만 함
            room.markImagesForDeletion();

        } else {
            // 신규 객실 생성
            room = createNewRoom(hotel, roomDto);
        }

        // 새 이미지 업로드 및 연결
        List<RoomImage> newImages = uploadRoomImagesFor(roomDto.getRoomKey(), roomImages, room, uploadedImageUrls);
        room.updateRoomImages(newImages);

        return room;
    }
    private Room createNewRoom(Hotel hotel, RoomUpdateDto roomDto) {
        return Room.builder()
                .hotel(hotel)
                .type(roomDto.getType())
                .roomCount(roomDto.getRoomCount())
                .weekPrice(roomDto.getWeekPrice())
                .weekendPrice(roomDto.getWeekendPrice())
                .standardPeople(roomDto.getStandardPeople())
                .maximumPeople(roomDto.getMaximumPeople())
                .roomOption1(roomDto.getRoomOption1())
                .roomOption2(roomDto.getRoomOption2())
                .checkIn(roomDto.getCheckIn())
                .checkOut(roomDto.getCheckOut())
                .describtion(roomDto.getDescribtion())
                .state(HotelState.APPLY)
                .build();
    }

    private List<RoomImage> uploadRoomImagesFor(String roomKey, List<MultipartFile> files,
                                                Room room, List<String> uploadedImageUrls) {
        return files.stream()
                .filter(f -> f.getOriginalFilename() != null && f.getOriginalFilename().startsWith(roomKey))
                .map(f -> {
                    String imageUrl = s3Uploader.upload(f, "room");
                    uploadedImageUrls.add(imageUrl); // 롤백을 위해 추적
                    return RoomImage.builder()
                            .room(room)
                            .imageUrl(imageUrl)
                            .build();
                })
                .toList();
    }

    private void markRemovedRooms(List<Room> rooms, Set<Long> processedRoomIds) {
        rooms.stream()
                .filter(room -> room.getId() != null && !processedRoomIds.contains(room.getId()))
                .forEach(room -> {
                    room.updateState(HotelState.REMOVE);
                    room.markImagesForDeletion();
                });
    }

    private void cleanupOldImages(Hotel hotel, String oldHotelImageUrl) {
        // 백그라운드에서 비동기로 처리하는 것이 좋음
        CompletableFuture.runAsync(() -> {
            try {
                // 호텔 이미지 삭제
                if (oldHotelImageUrl != null && !oldHotelImageUrl.equals(hotel.getImage())) {
                    s3Uploader.delete(oldHotelImageUrl);
                }

                // 삭제 마킹된 객실 이미지들 삭제
                hotel.getRooms().stream()
                        .filter(Room::hasImagesMarkedForDeletion)
                        .forEach(room -> {
                            List<String> imageUrls = room.getImageUrlsMarkedForDeletion();
                            s3Uploader.batchDelete(imageUrls); // 배치 삭제
                            room.clearDeletionMarks();
                        });

            } catch (Exception e) {
                log.warn("이미지 정리 중 오류 발생: {}", e.getMessage());
                // 실패해도 메인 로직에는 영향 없음
            }
        });
    }

    private void rollbackUploadedImages(List<String> uploadedImageUrls) {
        if (!uploadedImageUrls.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    s3Uploader.batchDelete(uploadedImageUrls);
                } catch (Exception e) {
                    log.error("이미지 롤백 실패: {}", e.getMessage());
                }
            });
        }
    }

    public void deleteHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("호텔 정보가 없습니다."));
        hotel.updateState(HotelState.REMOVE);
        for(Room r : hotel.getRooms()) {
            r.updateState(HotelState.REMOVE);
        }
    }
}
