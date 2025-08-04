package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.common.service.ReservationInventoryService;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.pay.domain.Payment;
import com.beyond.HanSoom.pay.repository.PaymentRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.reservation.dto.res.ReservationResDto;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.room.repository.RoomRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationInventoryService reservationInventoryService;
    public String confirm(ReservationReqDto dto) {
        // 값 유효성 검증
        User user = getUser();
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(()->new EntityNotFoundException("해당 호텔이 존재하지 않습니다."));;
        Room room = roomRepository.findByIdAndHotel(dto.getRoomId(),hotel).orElseThrow(()-> new EntityNotFoundException("해당 객실이 존재하지 않습니다."));

        // 예약 인원 검증
        if(dto.getPeople()>room.getMaximumPeople()){
            throw new IllegalStateException("인원이 초과 되었습니다.");
        }

        // 예약 가능 여부 검증
        List<Reservation> reservationList = reservationRepository.checkRoom(user,room,hotel, dto.getCheckIn(), dto.getCheckOut(), State.RESERVED);
        if(reservationList.size()>room.getRoomCount()){
            throw new IllegalArgumentException("빈 객실이 존재하지 않습니다.");
        }

        reservationInventoryService.getInventory(new ReservationDto().makeDto(hotel, room, dto.getCheckIn(), dto.getCheckOut(), room.getRoomCount()));

        // 실제 숙박비 계산
        LocalDate date = dto.getCheckIn();

        long totalPrice = 0;

        while(!date.isEqual(dto.getCheckOut())){
        DayOfWeek day = date.getDayOfWeek();
        if(day==SATURDAY || day==SUNDAY ){
            totalPrice += room.getWeekendPrice();
        }else{
            totalPrice += room.getWeekPrice();
        }
            date=date.plusDays(1);
        }

        return reservationRepository.save(dto.toEntity(totalPrice,user,hotel, room)).getUuid();

    }


    public List<ReservationResDto> find() {
        User user = getUser();
        List<Reservation> reservation = reservationRepository.findAllByUser(user);
        return reservation.stream().map(a-> new ReservationResDto().fromEntity(a)).collect(Collectors.toList());
    }



    public String cancel(Long reservationId){
        User user = getUser();
        Reservation reservation = reservationRepository.findByIdAndUser(reservationId,user);
        reservation.cancel();
        return reservation.getUuid();
    }

    public String complete(Long orderId) {
        Reservation reservation = reservationRepository.findById(orderId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 예약 내역 입니다."));
        Payment payment = paymentRepository.findByReservationId(reservation.getId());

        if(payment!=null && reservation.getState() == State.SUCCESS && payment.getState()==State.SUCCESS){
            reservation.changeState(State.RESERVED);
            return reservation.getUuid();
        }else{
            throw new IllegalStateException("결제가 완료되지 않은 주문 입니다.");
        }
    }

    private User getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        return user;
    }


    public Reservation findDetail() {
        return null;
    }
}
