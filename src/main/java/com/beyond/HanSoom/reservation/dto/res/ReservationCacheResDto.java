package com.beyond.HanSoom.reservation.dto.res;

import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationCacheResDto {
    private Long reservationId;
    private Long chatRoomId;
    private HotelDto hotelDto;
    private UserDto userDto;
    private ReservationDto reservationDto;
    private String status;
    private BigDecimal hotelRating;
    private int hotelReviewCount;
    private String roomType;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReservationDto{
        private String id;
        private String reservationId;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Long guests;
        private Long price;
        private String request;
        public static ReservationDto fromEntity(Reservation reservation){
            return ReservationDto.builder()
                    .id(reservation.getUuid())
                    .reservationId(reservation.getUuid())
                    .checkIn(reservation.getCheckInDate())
                    .checkOut(reservation.getCheckOutDate())
                    .guests(reservation.getPeople())
                    .price(reservation.getPrice())
                    .request(reservation.getRequest())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class HotelDto{
        private String name;
        private String address;
        private String phone;
        private String image;
        public HotelDto fromEntity(Hotel hotel){
            return HotelDto.builder()
                    .name(hotel.getHotelName())
                    .address(hotel.getAddress())
                    .phone(hotel.getPhoneNumber())
                    .image(hotel.getImage())
                    .build();
        }
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserDto{
        private String name;
        private String email;
        private String phone;
        public UserDto fromEntity(User user){
            return UserDto.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .build();
        }
    }

    // Entity -> DTO 변환 메서드
    public ReservationCacheResDto fromEntity(Reservation reservation, String status, BigDecimal hotelRating, int reviews, Long chatRoomId) {
        Hotel hotel = reservation.getHotel();
        Room room = reservation.getRoom();
        User user = reservation.getUser();
        Long sum = 0L;


        return ReservationCacheResDto.builder()
                .reservationId(reservation.getId())
                .chatRoomId(chatRoomId)
                .userDto(new UserDto().fromEntity(user))
                .hotelDto(new HotelDto().fromEntity(hotel))
                .reservationDto(new ReservationDto().fromEntity(reservation))
                .status(status)
                .hotelRating(hotelRating)
                .hotelReviewCount(reviews)
                .roomType(room.getType())
                .build();

    }

}

