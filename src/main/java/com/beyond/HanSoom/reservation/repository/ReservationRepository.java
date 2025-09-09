package com.beyond.HanSoom.reservation.repository;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    List<Reservation> findAllByUser(User user);
    Page<Reservation> findAllByUser(User user, Pageable pageable);

    Reservation findByUser(User user);

    //입력되는 날짜의 체크아웃 날짜 이전에 체크인 한 예약 정보 가져오기 ex ) 3~5일 예약이면 5일 이전에 체크한 값 이면서 , 입력값의 체크인 날짜 이후에 나가는 모든 값 = 5일 이전 값 중 3일 이후에 체크아웃하는 모든 예약 정보
//    @Query("select r from reservation r where r.hotel=:hotel and r.room=:room and r.user=:user and r.checkIn < :checkOut and r.checkOut > :checkIn and r.state=:state  ")
    @Query("""
    select r
    from Reservation r
    join fetch r.hotel
    join fetch r.room
    join fetch r.user
    where r.hotel = :hotel
      and r.room = :room
      and r.user = :user
      and r.checkInDate < :checkOut
      and r.checkOutDate > :checkIn
      and r.state = :state
""")
    List<Reservation> checkRoom(
            @Param("user") User user,
            @Param("room") Room room,
            @Param("hotel") Hotel hotel,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("state") State state
    );


    Reservation findByIdAndUser(Long reservationId, User user);

    Optional<Reservation> findByUuid(String orderId);


    List<Reservation> findAllByUserAndHotel(User user, Hotel hotel);

    List<Reservation> findAllByHotel(Hotel hostHotel);
}
