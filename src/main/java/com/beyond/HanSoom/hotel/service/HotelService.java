package com.beyond.HanSoom.hotel.service;

import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.common.service.ReservationInventoryService;
import com.beyond.HanSoom.common.service.S3Uploader;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelDocument;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.hotel.dto.*;
import com.beyond.HanSoom.hotel.repository.HotelDocumentRepository;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.notification.service.NotificationService;
import com.beyond.HanSoom.notification.service.SseAlarmService;
import com.beyond.HanSoom.review.domain.HotelReviewSummary;
import com.beyond.HanSoom.review.repository.HotelReviewSummaryRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.room.domain.RoomDocument;
import com.beyond.HanSoom.room.dto.RoomDetailResponseDto;
import com.beyond.HanSoom.room.dto.RoomDetailSearchResponseDto;
import com.beyond.HanSoom.room.dto.RoomUpdateDto;
import com.beyond.HanSoom.room.repository.RoomRepository;
import com.beyond.HanSoom.roomImage.domain.RoomImage;
import com.beyond.HanSoom.roomImage.dto.RoomImageResponseDto;
import com.beyond.HanSoom.roomImage.repository.RoomImageRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.processing.Completion;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HotelService {
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final S3Uploader s3Uploader;
    private final GeocoderService geocoderService;
    private final ReservationInventoryService reservationInventoryService;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final NotificationService notificationService;
    private final SseAlarmService sseAlarmService;
    private final HotelReviewSummaryRepository hotelReviewSummaryRepository;
    private final HotelDocumentRepository hotelDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final HotelSearchQueryBuilder hotelSearchQueryBuilder;


    public void registerHotel(HotelRegisterRequsetDto dto, MultipartFile hotelImage, List<MultipartFile> roomImages) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("등록된 사용자가 아닙니다."));

        // 호텔 주소 정규화
        dto.setAddress(normalizeAddress(dto.getAddress()));

        // 호텔 이미지 S3 저장
        String hotelImageUrl = (hotelImage != null && !hotelImage.isEmpty())
                ? s3Uploader.upload(hotelImage, "hotel")
                : null;
        // 호텔 객체 저장
        GeocoderService.HotelAddressDto addressDto = geocoderService.parseAddress(dto.getAddress());
        Hotel hotel = hotelRepository.save(dto.toEntity(hotelImageUrl, addressDto, user));

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

        Hotel savedHotel = hotelRepository.save(hotel);

        saveToElasticsearch(savedHotel, addressDto);

        // 호텔등록 알림 생성
        // 관리자 고정
        User admin = userRepository.findById(1L).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        notificationService.createNotiNewHotelSubmitted(admin, hotel);
        sseAlarmService.publishReserved(admin.getEmail(), "hotelSubmitted");

        hotelRepository.save(hotel);
    }

    // Elasticsearch 저장 로직 분리
    private void saveToElasticsearch(Hotel hotel, GeocoderService.HotelAddressDto addressDto) {
        try {
            // Room Entity를 RoomDocument로 변환
            List<RoomDocument> roomDocuments = hotel.getRooms().stream()
                    .map(room -> RoomDocument.builder()
                            .id(room.getId())
                            .maximumPeople(room.getMaximumPeople())
                            .state(room.getState().name()) // Enum을 String으로 변환
                            .build())
                    .collect(Collectors.toList());

            // HotelDocument 생성
            HotelDocument hotelDocument = HotelDocument.builder()
                    .id(hotel.getId())
                    .hotelName(hotel.getHotelName())
                    .addressCity(addressDto.getAddressCity())
                    .addressDetail(addressDto.getAddressDetail())
                    .address(addressDto.getAddressCity()+ " " +addressDto.getAddressDetail())
                    // suggest 필드에는 동일한 값을 저장 (Search As You Type이 자동으로 n-gram 처리)
                    .hotelNameSuggest(hotel.getHotelName())
                    .addressCitySuggest(addressDto.getAddressCity())
                    .state(hotel.getState().toString()) // Enum을 String으로 변환
                    .type(hotel.getType().toString())   // Enum을 String으로 변환
                    .rooms(roomDocuments)
                    .build();

            // Elasticsearch에 저장
            hotelDocumentRepository.save(hotelDocument);
            log.info("[HANSOOM][INFO] Elasticsearch에 호텔 인덱싱 완료: ID = {}, 호텔명 = {}",
                    hotel.getId(), hotel.getHotelName());

        } catch (Exception e) {
            log.error("[HANSOOM][ERROR] Elasticsearch 저장 실패: 호텔 ID = {}, 오류 = {}", hotel.getId(), e.getMessage());
            // Elasticsearch 저장 실패해도 JPA 저장은 유지 (선택적)
            // 만약 Elasticsearch 저장이 필수라면 RuntimeException을 던져서 전체 트랜잭션 롤백
        }
    }

    private String normalizeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return address;
        }

        String normalized = address;

        // "특별시"를 "시"로 변환 (예: 서울특별시 -> 서울시)
        normalized = normalized.replace("특별시", "시");

        // "광역시"를 "시"로 변환 (예: 부산광역시 -> 부산시)
        normalized = normalized.replace("광역시", "시");

        // "특별자치도"를 "도"로 변환 (예: 강원특별자치도 -> 강원도)
        normalized = normalized.replace("특별자치도", "도");

        // 불필요한 공백 제거
        return normalized.trim();
    }

    public void answerAdmin(HotelStateUpdateDto dto) {
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new EntityNotFoundException("등록된 호텔이 없습니다."));
        hotel.updateState(dto.getState());

        for(Room r : hotel.getRooms()) {
            r.updateState(dto.getState());
        }

        // 2. JPA 엔티티 저장 (데이터베이스에 반영)
        Hotel savedHotel = hotelRepository.save(hotel);

        GeocoderService.HotelAddressDto addressDto = geocoderService.parseAddress(hotel.getAddress());

        // 3. ElasticSearch Document 업데이트
        // JPA 엔티티의 최신 상태를 바탕으로 새로운 Document 객체 생성
        saveToElasticsearch(savedHotel, addressDto);
    }

    private int extractRoomIndex(String filename) {
        String prefix = filename.split("_")[0];     // room1
        return Integer.parseInt(prefix.replace("room", "")); // 0-indexed
    }

    public void updateHotelWithUrls(HotelUpdateDto dto, MultipartFile hotelImage, List<MultipartFile> roomImages, List<ImageDto> imageUrls) {
        // 1. 호텔 조회
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
        Hotel hotel = hotelRepository.findByUserAndState(user, HotelState.APPLY)
                .orElseThrow(() -> new EntityNotFoundException("호텔이 존재하지 않습니다."));

        try {
            // 2. 주소로 좌표 조회
            dto.setAddress(normalizeAddress(dto.getAddress()));
            GeocoderService.HotelAddressDto addressDto = geocoderService.parseAddress(dto.getAddress());

            // 3. 호텔 기본 정보 업데이트
            hotel.updateBasicInfo(dto.getHotelName(), dto.getAddress(),
                    dto.getPhoneNumber(), dto.getDescription(), dto.getType(),
                    addressDto.getLatitude(), addressDto.getLongitude());

            // 4. 호텔 이미지 업데이트
            updateHotelImage(hotel, hotelImage, imageUrls);

            // 5. 객실 정보 업데이트
            updateRoomsWithImages(hotel, dto.getRooms(), roomImages, imageUrls);

            // 6. 저장
            Hotel savedHotel = hotelRepository.save(hotel);

            // 7. Elastic Search Document 업데이트
            saveToElasticsearch(savedHotel, addressDto);

            log.info("[HANSOOM][INFO] 호텔 업데이트 성공 - ID: {}", hotel.getId());

        } catch (Exception e) {
            log.error("[HANSOOM][ERROR] 호텔 업데이트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("호텔 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 호텔 이미지 업데이트 처리
     */
    private void updateHotelImage(Hotel hotel, MultipartFile hotelImage, List<ImageDto> imageUrls) {
        String currentImageUrl = hotel.getImage();

        if (hotelImage != null && !hotelImage.isEmpty()) {
            String newImageUrl = s3Uploader.upload(hotelImage, "hotel");
            hotel.updateImage(newImageUrl);
            if (currentImageUrl != null) deleteImageAsync(currentImageUrl);
        } else if (imageUrls != null && !imageUrls.isEmpty()) {
            boolean keepCurrentImage = imageUrls.stream()
                    .filter(img -> "hotel".equals(img.getKey()))
                    .anyMatch(img -> img.getImageUrl().equals(currentImageUrl));
            if (!keepCurrentImage && currentImageUrl != null) {
                deleteImageAsync(currentImageUrl);
                hotel.updateImage(null);
            }
        }
    }

    /**
     * 객실 정보 및 이미지 업데이트
     */
    private void updateRoomsWithImages(Hotel hotel, List<RoomUpdateDto> roomDtos, List<MultipartFile> roomImages, List<ImageDto> imageUrls) {
        Map<Long, Room> existingRooms = hotel.getRooms().stream()
                .collect(Collectors.toMap(Room::getId, room -> room));

        List<Room> updatedRoomList = new ArrayList<>();
        Set<Long> updatedRoomIds = roomDtos.stream()
                .map(RoomUpdateDto::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (RoomUpdateDto dto : roomDtos) {
            Room room;
            if (dto.getRoomId() != null && existingRooms.containsKey(dto.getRoomId())) {
                // 기존 객실 업데이트
                room = existingRooms.get(dto.getRoomId());
                room.updateInfo(dto);

                Set<String> keepImageUrls = getKeepImageUrlsForRoom(imageUrls, dto.getRoomKey());
                List<RoomImage> imagesToKeep = new ArrayList<>();
                List<String> imagesToDelete = new ArrayList<>();

                if (room.getRoomImages() != null) {
                    for (RoomImage img : room.getRoomImages()) {
                        if (keepImageUrls.contains(img.getImageUrl())) {
                            imagesToKeep.add(img);
                        } else {
                            imagesToDelete.add(img.getImageUrl());
                        }
                    }
                }

                List<RoomImage> newImages = uploadRoomImages(dto.getRoomKey(), roomImages, room);
                imagesToKeep.addAll(newImages);
                room.updateRoomImages(imagesToKeep);
                deleteImagesAsync(imagesToDelete);

            } else {
                // 새 객실 생성
                room = Room.builder()
                        .hotel(hotel)
                        .type(dto.getType())
                        .roomCount(dto.getRoomCount())
                        .weekPrice(dto.getWeekPrice())
                        .weekendPrice(dto.getWeekendPrice())
                        .standardPeople(dto.getStandardPeople())
                        .maximumPeople(dto.getMaximumPeople())
                        .roomOption1(dto.getRoomOption1())
                        .roomOption2(dto.getRoomOption2())
                        .checkIn(dto.getCheckIn())
                        .checkOut(dto.getCheckOut())
                        .description(dto.getDescription())
                        .state(HotelState.APPLY)
                        .build();

                List<RoomImage> newImages = uploadRoomImages(dto.getRoomKey(), roomImages, room);
                room.updateRoomImages(newImages);
            }

            updatedRoomList.add(room);
        }

        // 삭제된 객실 처리
        handleDeletedRoomsDirect(hotel, updatedRoomIds);

        hotel.updateRooms(updatedRoomList);
    }

    /**
     * DTO에 없는 기존 객실은 REMOVE 상태로 업데이트하고 이미지 삭제
     */
    private void handleDeletedRoomsDirect(Hotel hotel, Set<Long> updatedRoomIds) {
        List<Room> roomsToRemove = hotel.getRooms().stream()
                .filter(r -> !updatedRoomIds.contains(r.getId()))
                .collect(Collectors.toList());

        for (Room room : roomsToRemove) {
            room.updateState(HotelState.REMOVE);

            // RoomImageRepository에서 직접 가져와 삭제
            List<RoomImage> roomImages = roomImageRepository.findByRoomId(room.getId());
            List<String> imageUrls = roomImages.stream()
                    .map(RoomImage::getImageUrl)
                    .collect(Collectors.toList());
            deleteImagesAsync(imageUrls);
            room.getRoomImages().clear();

            log.info("[HANSOOM][INFO] 삭제 처리된 객실 - ID: {}", room.getId());
        }
    }

    /**
     * 특정 객실에 대해 유지할 이미지 URL들 반환
     */
    private Set<String> getKeepImageUrlsForRoom(List<ImageDto> imageUrls, String roomKey) {
        if (imageUrls == null || imageUrls.isEmpty()) return new HashSet<>();
        return imageUrls.stream()
                .filter(img -> roomKey.equals(img.getKey()))
                .map(ImageDto::getImageUrl)
                .collect(Collectors.toSet());
    }

    /**
     * 객실 이미지 업로드
     */
    private List<RoomImage> uploadRoomImages(String roomKey, List<MultipartFile> files, Room room) {
        if (files == null || files.isEmpty()) return new ArrayList<>();
        return files.stream()
                .filter(f -> f.getOriginalFilename() != null && f.getOriginalFilename().startsWith(roomKey))
                .map(f -> {
                    String url = s3Uploader.upload(f, "room");
                    return RoomImage.builder().room(room).imageUrl(url).build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 이미지 비동기 삭제
     */
    private void deleteImagesAsync(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
            for (String url : imageUrls) {
                try {
                    s3Uploader.delete(url);
                    log.info("[HANSOOM][INFO] 이미지 삭제 성공: {}", url);
                } catch (Exception e) {
                    log.error("[HANSOOM][ERROR] 이미지 삭제 실패: {}", url, e);
                }
            }
        });
    }

    /**
     * 단일 이미지 비동기 삭제
     */
    private void deleteImageAsync(String imageUrl) {
        if (imageUrl == null) return;
        CompletableFuture.runAsync(() -> {
            try {
                s3Uploader.delete(imageUrl);
                log.info("[HANSOOM][INFO] 이미지 삭제 성공: {}", imageUrl);
            } catch (Exception e) {
                log.error("[HANSOOM][ERROR] 이미지 삭제 실패: {}", imageUrl, e);
            }
        });
    }



    //    호텔 삭제 : 상태 REMOVE로 변경
    public void deleteHotel() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
        Hotel hotel = hotelRepository.findByUserAndState(user, HotelState.APPLY)
                .orElseThrow(() -> new EntityNotFoundException("호텔이 존재하지 않습니다."));
        hotel.updateState(HotelState.REMOVE);
        s3Uploader.delete(hotel.getImage());
        for(Room r : hotel.getRooms()) {
            r.updateState(HotelState.REMOVE);
            List<String> imageUrls = new ArrayList<>();
            for(RoomImage roomImage : r.getRoomImages()) {
                imageUrls.add(roomImage.getImageUrl());
            }
            s3Uploader.batchDelete(imageUrls);
            r.getRoomImages().clear();
        }
    }

//    호텔 단건 조회
    @Transactional(readOnly = true)
    public HotelDetailSearchResponseDto findById(Long id, HotelDetailSearchDto searchDto) {
        Hotel hotel = hotelRepository.findByIdAndState(id, HotelState.APPLY).orElseThrow(() -> new EntityNotFoundException("호텔 정보가 없습니다."));
        List<RoomDetailSearchResponseDto> roomDto = new ArrayList<>();
        for(Room r : hotel.getRooms()) {
            if(r.getState()==HotelState.REMOVE) continue;
            if(searchDto.getPeople() > r.getMaximumPeople()) continue;

            ReservationDto reservationDto = ReservationDto.builder()
                    .hotelId(hotel.getId())
                    .roomId(r.getId())
                    .checkIn(searchDto.getCheckIn())
                    .checkOut(searchDto.getCheckOut())
                    .maxStock(r.getRoomCount())
                    .build();
            System.out.println(reservationDto);
            int remainRoom = reservationInventoryService.getInventory(reservationDto);
            System.out.println("========remainRoom");
            System.out.println(remainRoom);
            if(remainRoom == 0) continue;

            List<RoomImageResponseDto> roomImages = r.getRoomImages().stream().map(a -> RoomImageResponseDto.fromEntity(a)).toList();
//            roomDto.add(RoomDetailSearchResponseDto.fromEntity(r, roomImages, remainRoom));
            int price = calculateAveragePrice(r, searchDto.getCheckIn(), searchDto.getCheckOut());
            roomDto.add(RoomDetailSearchResponseDto.fromEntity(r, roomImages, remainRoom, price));
        }
        return HotelDetailSearchResponseDto.fromEntity(hotel, roomDto);
    }

//    호스트의 내 호텔 조회
    public HotelDetailResponseDto myHotel() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("등록된 사용자가 없습니다."));

        Hotel hotel = hotelRepository.findTopByUserAndState(user, HotelState.APPLY).orElseThrow(() -> new EntityNotFoundException("호텔 정보가 없습니다."));
        List<RoomDetailResponseDto> roomDto = new ArrayList<>();
        for(Room r : hotel.getRooms()) {
            if(r.getState()==HotelState.REMOVE) continue;

            List<RoomImageResponseDto> roomImages = r.getRoomImages().stream().map(a -> RoomImageResponseDto.fromEntity(a)).toList();
            roomDto.add(RoomDetailResponseDto.fromEntity(r, roomImages, r.getRoomCount()));
        }
        return HotelDetailResponseDto.fromEntity(hotel, roomDto);
    }

    public HotelStateUpdateDto myHotelCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("등록된 사용자가 없습니다."));

        int count = hotelRepository.countByUser(user);
        if(count==0) return HotelStateUpdateDto.builder().hotelId(0L).build();
        Hotel hotel = hotelRepository.findByUser(user);

        return HotelStateUpdateDto.builder().hotelId(hotel.getId()).state(hotel.getState()).build();
    }

//    Admin의 호텔 단건 조회
    public HotelDetailResponseDto findHotelAdmin(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("호텔 정보가 없습니다."));
        List<RoomDetailResponseDto> roomDto = new ArrayList<>();
        for(Room r : hotel.getRooms()) {
            List<RoomImageResponseDto> roomImages = r.getRoomImages().stream().map(a -> RoomImageResponseDto.fromEntity(a)).toList();
            roomDto.add(RoomDetailResponseDto.fromEntity(r, roomImages, r.getRoomCount()));
        }
        return HotelDetailResponseDto.fromEntity(hotel, roomDto);
    }

//    Admin 조회
    @Transactional(readOnly = true)
    public Page<HotelListAdminResponseDto> findAllAdmin(Pageable pageable) {
        Page<Hotel> hotels = hotelRepository.findAll(pageable);
        return hotels.map(HotelListAdminResponseDto::fromEntity);
    }
//    Admin 조회(WAIT상태만)
    public Page<HotelListAdminResponseDto> findWait(Pageable pageable) {
        Page<Hotel> hotels = hotelRepository.findByState(pageable, HotelState.WAIT);
        return hotels.map(HotelListAdminResponseDto::fromEntity);
    }

//    고객 조회
    public Page<HotelListResponseDto> findAll(Pageable pageable, HotelListSearchDto searchDto) {
        // 1. Specification을 사용해 DB에서 1차 필터링된 호텔 페이지를 가져옵니다.
        // 이 쿼리는 Pageable 정보를 사용해 페이징 처리가 완료된 상태입니다.
        searchDto.setAddress(normalizeAddress(searchDto.getAddress()));
        Specification<Hotel> spec = HotelSpecification.withSearchConditions(searchDto);
        Page<Hotel> hotelPage = hotelRepository.findAll(spec, pageable);

        // 2. Page<Hotel>의 getContent()로 가져온 리스트에 대해 추가 비즈니스 로직 필터링 및 DTO 변환을 수행합니다.
        List<HotelListResponseDto> dtoList = new ArrayList<>(hotelPage.getContent().stream()
                // 호텔에 객실이 존재하는지, 그리고 해당 객실에 예약 가능한 재고가 있는지 확인합니다.
                .filter(hotel -> hotel.getRooms().stream().anyMatch(room ->
                        isRoomAvailable(room, hotel.getId(), searchDto)
                ))
                // 각 호텔별로 가장 저렴한 객실 가격을 찾아 DTO로 변환합니다.
                .flatMap(hotel -> {
                    // 조건에 맞는 객실들 중 평균가가 가장 저렴한 객실을 찾습니다.
                    OptionalInt minAvgPrice = hotel.getRooms().stream()
                            .filter(room -> isRoomAvailable(room, hotel.getId(), searchDto))
                            .mapToInt(room -> calculateAveragePrice(room, searchDto.getCheckIn(), searchDto.getCheckOut()))
                            .filter(avgPrice ->
                                    (avgPrice >= searchDto.getMinPrice()) && (avgPrice <= searchDto.getMaxPrice())
                            )
                            .min();

                    // 평점 필터링
                    if (searchDto.getRating() != null) {
                        if (hotel.getHotelReviewSummary().getAverage().compareTo(searchDto.getRating()) < 0) {
                            return Stream.empty();
                        }
                    }

                    return minAvgPrice.isPresent()
                            ? Stream.of(HotelListResponseDto.fromEntity(hotel, minAvgPrice.getAsInt()))
                            : Stream.empty();
                })
                .toList());

        // 3. 정렬 조건에 따라 결과 리스트를 정렬합니다.
        // 이 정렬은 DB에서 처리하는 것이 아니므로, 결과 리스트를 직접 정렬해야 합니다.
        if (!dtoList.isEmpty() && searchDto.getSortOption() != null) {
            String[] sortParams = searchDto.getSortOption().split(",");
            String sortBy = sortParams[0];
            String sortDirection = sortParams[1];
            Comparator<HotelListResponseDto> comparator = null;

            if ("price".equals(sortBy)) {
                comparator = Comparator.comparing(HotelListResponseDto::getPrice);
            } else if ("rating".equals(sortBy)) {
                comparator = Comparator.comparing(HotelListResponseDto::getRating, Comparator.nullsLast(BigDecimal::compareTo));
            }

            if (comparator != null) {
                if ("desc".equals(sortDirection)) {
                    comparator = comparator.reversed();
                }
                dtoList.sort(comparator);
            }
        }

        // 4. `PageImpl`을 생성할 때, 전체 데이터의 개수는 `hotelPage.getTotalElements()`를 사용합니다.
        // 이렇게 하면 정확한 페이지네이션 정보(총 페이지 수, 총 데이터 개수)를 제공할 수 있습니다.
        return new PageImpl<>(dtoList, pageable, hotelPage.getTotalElements());
    }

    /**
     * 객실 상태, 인원수, 예약 가능 재고를 확인하는 헬퍼 메서드
     */
    private boolean isRoomAvailable(Room room, Long hotelId, HotelListSearchDto searchDto) {
        if (room.getState() == HotelState.REMOVE) {
            return false;
        }
        if (room.getMaximumPeople() < searchDto.getPeople()) {
            return false;
        }

        ReservationDto dto = ReservationDto.builder()
                .hotelId(hotelId)
                .roomId(room.getId())
                .checkIn(searchDto.getCheckIn())
                .checkOut(searchDto.getCheckOut())
                .maxStock(room.getRoomCount())
                .build();

        int available = reservationInventoryService.getInventory(dto);
        return available > 0;
    }

    // 개선된 고객 조회 메서드
    public Page<HotelListResponseDto> findByElasticsearch(Pageable pageable, HotelListSearchDto searchDto) {
        log.info("=== ElasticSearch 검색 시작 ===");
        log.info("[HANSOOM][INFO] searchDto: {}", searchDto);

        // 1. Elasticsearch에서 전체 검색 (큰 사이즈로 한 번에 가져오기)
        final int maxSize = 10000; // 충분히 큰 크기
        Pageable largePageable = PageRequest.of(0, maxSize);

        Query searchQuery = hotelSearchQueryBuilder.buildImprovedFuzzySearchQuery(searchDto);
        searchQuery.setPageable(largePageable);
        SearchHits<HotelDocument> searchHits = elasticsearchOperations.search(searchQuery, HotelDocument.class);
        log.info("[HANSOOM][INFO] 1단계 정확한 검색 결과: {}", searchHits.getTotalHits());

        // 오타 대응 검색
        if (searchHits.getTotalHits() == 0) {
            log.info("[HANSOOM][INFO] 2단계 오타 대응 검색 시작");
            if (searchDto.getHotelName() != null) {
                searchQuery = hotelSearchQueryBuilder.buildFlexibleHotelNameQuery(searchDto);
            } else {
                searchQuery = hotelSearchQueryBuilder.buildFlexibleAddressQuery(searchDto);
            }
            searchQuery.setPageable(largePageable);
            searchHits = elasticsearchOperations.search(searchQuery, HotelDocument.class);
            log.info("[HANSOOM][INFO] 2단계 오타 대응 검색 결과: {}", searchHits.getTotalHits());
        }

        log.info("[HANSOOM][INFO] ElasticSearch 검색 결과 총 개수: {}", searchHits.getTotalHits());

        // 검색된 각 결과의 상세 정보 로그
        searchHits.getSearchHits().forEach(hit -> {
            HotelDocument doc = hit.getContent();
            log.info("[HANSOOM][INFO] 검색된 호텔 - ID: {}, 이름: '{}', 도시: '{}', 상세주소: '{}'",
                    doc.getId(), doc.getHotelName(), doc.getAddressCity(), doc.getAddressDetail());
        });

        // 2. 조건에 맞는 전체 호텔 ID 필터링
        List<Long> filteredHotelIds = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(hotelDoc -> hotelDoc.getRooms().stream()
                        .anyMatch(room -> "APPLY".equals(room.getState()) &&
                                room.getMaximumPeople() >= searchDto.getPeople()))
                .map(HotelDocument::getId)
                .toList();

        log.info("[HANSOOM][INFO] 추출된 호텔 ID 목록: {}", filteredHotelIds);
        log.info("[HANSOOM][INFO] 필터링된 전체 호텔 개수: {}", filteredHotelIds.size());
        log.info("=== ElasticSearch 검색 완료 ===");

        // 빈 결과 처리
        if (filteredHotelIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 3. 전체 호텔 정보를 JPA로 조회 (페치 조인으로 N+1 문제 해결)
        List<Hotel> allHotels = hotelRepository.findByIdInWithRoomsAndReviewSummary(filteredHotelIds);

        // 4. 전체 DTO 변환 및 2차 필터링
        List<HotelListResponseDto> allDtoList = allHotels.stream()
                .filter(hotel -> hasAvailableRooms(hotel, searchDto))
                .map(hotel -> createHotelDto(hotel, searchDto))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // 5. ElasticSearch 정렬 순서 유지
        Map<Long, HotelListResponseDto> dtoMap = allDtoList.stream()
                .collect(Collectors.toMap(HotelListResponseDto::getId, Function.identity()));

        List<HotelListResponseDto> orderedAllResult = filteredHotelIds.stream()
                .map(dtoMap::get)
                .filter(Objects::nonNull)
                .toList();

        // 6. 전체 리스트에 정렬 적용
        List<HotelListResponseDto> sortedAllResult = applySorting(orderedAllResult, searchDto.getSortOption());

        int totalCount = sortedAllResult.size();
        log.info("[HANSOOM][INFO] 최종 정렬된 전체 호텔 개수: {}", totalCount);

        // 7. 정렬된 결과에서 페이징 처리
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), totalCount);

        // 인덱스 범위 검증
        if (startIndex >= totalCount) {
            return new PageImpl<>(Collections.emptyList(), pageable, totalCount);
        }

        List<HotelListResponseDto> pagedResult = sortedAllResult.subList(startIndex, endIndex);

        log.info("[HANSOOM][INFO] 페이지 {}, 총 {}개 중 {}개 반환",
                pageable.getPageNumber(), totalCount, pagedResult.size());

        return new PageImpl<>(pagedResult, pageable, totalCount);
    }

    private List<HotelListResponseDto> applySorting(List<HotelListResponseDto> hotels, String sortOption) {
        if (StringUtils.isEmpty(sortOption)) {
            return hotels;
        }

        switch (sortOption) {
            case "price,asc":
                return hotels.stream()
                        .sorted(Comparator.comparingInt(HotelListResponseDto::getPrice))
                        .collect(Collectors.toList());
            case "price,desc":
                return hotels.stream()
                        .sorted(Comparator.comparingInt(HotelListResponseDto::getPrice).reversed())
                        .collect(Collectors.toList());
            case "rating,desc":
                return hotels.stream()
                        .sorted(Comparator.comparing(HotelListResponseDto::getRating).reversed())
                        .collect(Collectors.toList());
            case "rating,asc":
                return hotels.stream()
                        .sorted(Comparator.comparing(HotelListResponseDto::getRating))
                        .collect(Collectors.toList());
            default:
                return hotels;
        }
    }

    public Page<HotelListResponseDto> findAllWishList(Pageable pageable) {
        Page<HotelListResponseDto> hotelListResponseDtoPage = hotelRepository.findAll(pageable).map(h -> HotelListResponseDto.fromEntity(h, 0));
        return hotelListResponseDtoPage;
    }

    // 헬퍼 메서드들로 가독성 향상
    private boolean hasAvailableRooms(Hotel hotel, HotelListSearchDto searchDto) {
        return hotel.getRooms().stream()
                .anyMatch(room -> isRoomAvailable(room, hotel.getId(), searchDto));
    }

    private Optional<HotelListResponseDto> createHotelDto(Hotel hotel, HotelListSearchDto searchDto) {
        // 평점 필터링
        if (searchDto.getRating() != null &&
                hotel.getHotelReviewSummary().getAverage().compareTo(searchDto.getRating()) < 0) {
            return Optional.empty();
        }

        // 가격 계산 및 필터링
        OptionalInt minAvgPrice = hotel.getRooms().stream()
                .filter(room -> isRoomAvailable(room, hotel.getId(), searchDto))
                .mapToInt(room -> calculateAveragePrice(room, searchDto.getCheckIn(), searchDto.getCheckOut()))
                .filter(avgPrice -> isPriceInRange(avgPrice, searchDto))
                .min();

        return minAvgPrice.isPresent()
                ? Optional.of(HotelListResponseDto.fromEntity(hotel, minAvgPrice.getAsInt()))
                : Optional.empty();
    }

    private boolean isPriceInRange(int avgPrice, HotelListSearchDto searchDto) {
        return avgPrice >= searchDto.getMinPrice() && avgPrice <= searchDto.getMaxPrice();
    }



//    가까운 호텔 조회
    @Transactional(readOnly = true)
    public Page<HotelLocationListResponseDto> findNearbyHotels(LocationHotelSearchDto dto, Pageable pageable) {
        // 1. 전체 호텔 개수를 먼저 가져옵니다.
        // 이는 페이지네이션의 'total' 값을 위해 필요합니다.
        long totalCount = hotelRepository.countNearbyHotels(dto.getLatitude(), dto.getLongitude(), 5);

        // 2. 현재 페이지에 해당하는 호텔 데이터를 거리를 기준으로 가져옵니다.
        List<Object[]> results = hotelRepository.findNearbyHotelsWithDistance(
                dto.getLatitude(), dto.getLongitude(), 5, pageable);

        List<HotelLocationListResponseDto> dtoList = new ArrayList<>();

        // 3. 가져온 각 호텔에 대해 비즈니스 로직 필터링을 적용합니다.
        for (Object[] row : results) {
            Long id = ((Number) row[0]).longValue();

            // 추가 필터링 로직을 수행하기 전에 hotelId로 객실과 평점 데이터를 가져옵니다.
            List<Room> availableRooms = roomRepository.findByHotelId(id).stream()
                    .filter(room -> room.getState() != HotelState.REMOVE)
                    .filter(room -> room.getMaximumPeople() >= dto.getPeople())
                    .filter(room -> {
                        ReservationDto reservationDto = ReservationDto.builder()
                                .hotelId(id)
                                .roomId(room.getId())
                                .checkIn(dto.getCheckIn())
                                .checkOut(dto.getCheckOut())
                                .maxStock(room.getRoomCount())
                                .build();
                        int remaining = reservationInventoryService.getInventory(reservationDto);
                        return remaining > 0;
                    })
                    .toList();

            if (!availableRooms.isEmpty()) {
                // 조건에 맞는 방이 있는 경우에만 DTO 생성
                String hotelName = (String) row[3];
                String address = (String) row[4];
                String image = (String) row[5];
                double latitude = ((Number) row[1]).doubleValue();
                double longitude = ((Number) row[2]).doubleValue();
                double rawDistance = ((Number) row[6]).doubleValue();
                double distance = Math.round(rawDistance * 100.0) / 100.0;

                HotelReviewSummary hotelReviewSummary = hotelReviewSummaryRepository.findByHotelId(id);

                int minAvgPrice = availableRooms.stream()
                        .mapToInt(room -> calculateAveragePrice(room, dto.getCheckIn(), dto.getCheckOut()))
                        .min()
                        .orElse(0);

                dtoList.add(HotelLocationListResponseDto.builder()
                        .id(id)
                        .hotelName(hotelName)
                        .address(address)
                        .image(image)
                        .latitude(latitude)
                        .longitude(longitude)
                        .distance(distance)
                        .price(minAvgPrice)
                        .rating(hotelReviewSummary.getAverage())
                        .reviewCount(hotelReviewSummary.getRatingCount())
                        .build());
            }
        }

        // 4. PageImpl을 생성할 때, dtoList와 pageable, 그리고 전체 개수(totalCount)를 사용합니다.
        return new PageImpl<>(dtoList, pageable, totalCount);
    }

    private int calculateAveragePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        int totalPrice = 0;
        int dayCount = 0;

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

            totalPrice += isWeekend ? room.getWeekendPrice() : room.getWeekPrice();
            dayCount++;
        }

        if (dayCount == 0) return 0; // 방어 코드
        return totalPrice / dayCount;
    }

    public List<HotelListResponseDto> popularHotel(HotelPopularRequestDto searchDto) {
        List<Hotel> hotels = hotelRepository.findTop30ByStateOrderByReservationCountDesc(HotelState.APPLY);;

        List<HotelListResponseDto> dtoList = new ArrayList<>(hotels.stream()
                // 호텔에 객실이 존재하는지, 그리고 해당 객실에 예약 가능한 재고가 있는지 확인합니다.
                .filter(hotel -> hotel.getRooms().stream().anyMatch(room ->
                        isRoomAvailablePop(room, hotel.getId(), searchDto)
                ))
                // 각 호텔별로 가장 저렴한 객실 가격을 찾아 DTO로 변환합니다.
                .flatMap(hotel -> {
                    // 조건에 맞는 객실들 중 평균가가 가장 저렴한 객실을 찾습니다.
                    OptionalInt minAvgPrice = hotel.getRooms().stream()
                            .filter(room -> isRoomAvailablePop(room, hotel.getId(), searchDto))
                            .mapToInt(room -> calculateAveragePrice(room, searchDto.getCheckIn(), searchDto.getCheckOut()))
                            .min();

                    return minAvgPrice.isPresent()
                            ? Stream.of(HotelListResponseDto.fromEntity(hotel, minAvgPrice.getAsInt()))
                            : Stream.empty();
                })
                .limit(10)
                .toList());

        return dtoList;
    }

    public List<HotelListResponseDto> popularPlaceHotel(HotelPopularRequestDto searchDto) {
        Specification<Hotel> spec = HotelSpecification.withSearchConditionsPop(searchDto);
        Pageable pageable = PageRequest.of(0, 30, Sort.by("reservationCount").descending());
        List<Hotel> hotels = hotelRepository.findAll(spec, pageable).getContent();

        List<HotelListResponseDto> dtoList = new ArrayList<>(hotels.stream()
                // 호텔에 객실이 존재하는지, 그리고 해당 객실에 예약 가능한 재고가 있는지 확인합니다.
                .filter(hotel -> hotel.getRooms().stream().anyMatch(room ->
                        isRoomAvailablePop(room, hotel.getId(), searchDto)
                ))
                // 각 호텔별로 가장 저렴한 객실 가격을 찾아 DTO로 변환합니다.
                .flatMap(hotel -> {
                    // 조건에 맞는 객실들 중 평균가가 가장 저렴한 객실을 찾습니다.
                    OptionalInt minAvgPrice = hotel.getRooms().stream()
                            .filter(room -> isRoomAvailablePop(room, hotel.getId(), searchDto))
                            .mapToInt(room -> calculateAveragePrice(room, searchDto.getCheckIn(), searchDto.getCheckOut()))
                            .min();

                    return minAvgPrice.isPresent()
                            ? Stream.of(HotelListResponseDto.fromEntity(hotel, minAvgPrice.getAsInt()))
                            : Stream.empty();
                })
                .limit(4)
                .toList());

        return dtoList;
    }

    private boolean isRoomAvailablePop(Room room, Long hotelId, HotelPopularRequestDto searchDto) {
        if (room.getState() == HotelState.REMOVE) {
            return false;
        }
        if (room.getMaximumPeople() < searchDto.getPeople()) {
            return false;
        }

        ReservationDto dto = ReservationDto.builder()
                .hotelId(hotelId)
                .roomId(room.getId())
                .checkIn(searchDto.getCheckIn())
                .checkOut(searchDto.getCheckOut())
                .maxStock(room.getRoomCount())
                .build();

        int available = reservationInventoryService.getInventory(dto);
        return available > 0;
    }


    public List<AutoCompleteSuggestion> getAutoCompleteSuggestions(String query, String searchType, int size) {
        try {
            Query searchQuery = hotelSearchQueryBuilder.buildAutoCompleteQuery(query, searchType, size);


            SearchHits<HotelDocument> searchHits = elasticsearchOperations.search(
                    searchQuery, HotelDocument.class, IndexCoordinates.of("hotels")
            );

            return searchHits.getSearchHits().stream()
                    .map(hit -> convertToSuggestion(hit, searchType))
                    .filter(Objects::nonNull)
                    .filter(suggestion -> suggestion.getText() != null && !suggestion.getText().trim().isEmpty())
                    .distinct() // distinct() 메서드로 중복을 제거
                    .limit(size)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private AutoCompleteSuggestion convertToSuggestion(SearchHit<HotelDocument> hit, String searchType) {
        HotelDocument hotel = hit.getContent();
        String text = getTextBySearchType(hotel, searchType);

        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        return AutoCompleteSuggestion.builder()
                .text(text.trim())
                .highlightedText(text.trim())
                .build();
    }

    private String getTextBySearchType(HotelDocument hotel, String searchType) {
        switch (searchType) {
            case "hotel_name":
                return hotel.getHotelName();
            case "address":
                return hotel.getAddressCity();
            default:
                return null;
        }
    }
}
