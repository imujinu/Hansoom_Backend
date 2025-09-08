package com.beyond.HanSoom.hotel.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.hotel.dto.*;
import com.beyond.HanSoom.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotel")
public class HotelController {

    private final HotelService hotelService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> registerHotel(@RequestPart(name = "hotelRegisterDto") HotelRegisterRequsetDto dto,
                                           @RequestPart(name = "hotelImage", required = false) MultipartFile hotelImage,
                                           @RequestPart(name = "roomImages", required = false) List<MultipartFile> roomImages) {
        hotelService.registerHotel(dto, hotelImage, roomImages);
        return new ResponseEntity<>(new CommonSuccessDto("OK", HttpStatus.CREATED.value(), "hotel is created"), HttpStatus.CREATED);
    }

    @PostMapping("/answerAdmin")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/myhotel")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> updateHotel(
            @RequestPart(name = "hotelUpdateDto") HotelUpdateDto dto,
            @RequestPart(name = "hotelImage", required = false) MultipartFile hotelImage,
            @RequestPart(name = "roomImages", required = false) List<MultipartFile> roomImages,
            @RequestPart(name = "imageUrls", required = false) List<ImageDto> imageUrls) {

        hotelService.updateHotelWithUrls(dto, hotelImage, roomImages, imageUrls);

        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 정보 수정")
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/myhotel")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> deleteHotel() {
        hotelService.deleteHotel();
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 삭제 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id, HotelDetailSearchDto searchDto) {
        HotelDetailSearchResponseDto dto = hotelService.findById(id, searchDto);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 정보 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/myhotel")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> myHotel() {
        HotelDetailResponseDto dto = hotelService.myHotel();
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/myhotelfind")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> myHotelFind() {
        HotelStateUpdateDto dto = hotelService.myHotelCount();
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOST')")
    public ResponseEntity<?> findHotelAdmin(@PathVariable Long id) {
        HotelDetailResponseDto dto = hotelService.findHotelAdmin(id);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAllAdmin(Pageable pageable) {
        Page<HotelListAdminResponseDto> dto = hotelService.findAllAdmin(pageable);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("관리자 호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/admin/waitlist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findWait(Pageable pageable) {
        Page<HotelListAdminResponseDto> dto = hotelService.findWait(pageable);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("관리자 호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(Pageable pageable, HotelListSearchDto searchDto) {
        Page<HotelListResponseDto> dto = hotelService.findByElasticsearch(pageable, searchDto);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/wishlist")
    public ResponseEntity<?> findAllWishList(@PageableDefault(size = 10) Pageable pageable) {
        Page<HotelListResponseDto> dto = hotelService.findAllWishList(pageable);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(dto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("호텔 위시리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyHotels( @PageableDefault(size = 10) Pageable pageable,
                                               LocationHotelSearchDto searchDto
    ) {

        Page<HotelLocationListResponseDto> result = hotelService.findNearbyHotels(searchDto, pageable);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(result)
                        .status_code(HttpStatus.OK.value())
                        .status_message("가까운 호텔 리스트 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<?> popularHotel(HotelPopularRequestDto dto) {
        List<HotelListResponseDto> result = hotelService.popularHotel(dto);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(result)
                        .status_code(HttpStatus.OK.value())
                        .status_message("인기호텔")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/place")
    public ResponseEntity<?> popularPlaceHotel(HotelPopularRequestDto dto) {
        List<HotelListResponseDto> result = hotelService.popularPlaceHotel(dto);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(result)
                        .status_code(HttpStatus.OK.value())
                        .status_message("인기지역")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/suggest")
    public ResponseEntity<?> getAutoComplete(
            @RequestParam("query") String query,
            @RequestParam("type") String searchType,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<AutoCompleteSuggestion> suggestions = hotelService.getAutoCompleteSuggestions(
                query.trim(), searchType, size
        );

        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(suggestions)
                        .status_code(HttpStatus.OK.value())
                        .status_message("자동완성")
                        .build(),
                HttpStatus.OK
        );
    }

}
