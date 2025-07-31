package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.room.repository.RoomRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {
    private ReservationRepository reservationRepository;
    private UserRepository userRepository;
    private RoomRepository roomRepository;
    private HotelRepository hotelRepository;

    public UUID confirm(ReservationReqDto dto) {
        // 값 유효성 검증
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(()->new EntityNotFoundException("해당 호텔이 존재하지 않습니다."));;
        Room room = roomRepository.findByIdAndHotel(dto.getRoomId(),hotel).orElseThrow(()-> new EntityNotFoundException("해당 객실이 존재하지 않습니다."));

        // 예약 인원 검증
        if(dto.getPeople()>room.getMaximumPeople()){
            throw new IllegalStateException("인원이 초과 되었습니다.");
        }

        // 예약 가능 여부 검증
        int roomStock = reservationRepository.checkRoom(user,room,hotel, dto.getCheckIn(), dto.getCheckOut());
        if(roomStock>room.getRoomCount()){
            throw new IllegalArgumentException("빈 객실이 존재하지 않습니다.");
        }

        // 실제 숙박비 계산
        LocalDate date = dto.getCheckIn();
        DayOfWeek day = date.getDayOfWeek();
        long totalPrice = 0;

        while(!date.isEqual(dto.getCheckOut())){

        if(day==SATURDAY || day==SUNDAY ){
            totalPrice += room.getWeekendPrice();
        }else{
            totalPrice += room.getWeekPrice();
        }
            date=date.plusDays(1);
        }

        return reservationRepository.save(dto.toEntity(totalPrice,user,hotel, room)).getId();

    }


    public List<ReservationResDto> find() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        List<Reservation> reservation = reservationRepository.findAllByUser(user);
        return reservation.stream().map(a-> new ReservationResDto().fromEntity(a)).collect(Collectors.toList());
    }

    public UUID cancel(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        Reservation reservation = reservationRepository.findByUser(user);
        reservation.cancel();
        return reservation.getId();
    }
}
