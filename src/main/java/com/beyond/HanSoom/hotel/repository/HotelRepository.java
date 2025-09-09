package com.beyond.HanSoom.hotel.repository;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Page<Hotel> findAll(Pageable pageable);
    Page<Hotel> findAll(Specification<Hotel> spec, Pageable pageable);

    Optional<Hotel> findByUserAndState(User user, HotelState hotelState);

    Optional<Hotel> findTopByUserAndState(User user, HotelState hotelState);

    Optional<Hotel> findByIdAndState(Long id, HotelState hotelState);


    @Query(value = """
    SELECT h.id, h.latitude, h.longitude, h.hotel_name, h.address, h.image,
           (6371 * acos(
               cos(radians(:lat)) * cos(radians(h.latitude)) *
               cos(radians(h.longitude) - radians(:lng)) +
               sin(radians(:lat)) * sin(radians(h.latitude))
           )) AS distance
    FROM hotel h
    WHERE h.state = 'APPLY'
    HAVING distance <= :radius
    ORDER BY distance
    LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
""", nativeQuery = true)
    List<Object[]> findNearbyHotelsWithDistance(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius,
            Pageable pageable);

    @Query(value = "SELECT count(*) " +
            "FROM hotel h WHERE h.state = 'APPLY' " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(h.latitude)) * cos(radians(h.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(h.latitude)))) <= :radius",
            nativeQuery = true)
    long countNearbyHotels(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") double radius);

    Page<Hotel> findByState(Pageable pageable, HotelState hotelState);

    List<Hotel> findAllByUser(User host);

    int countByUser(User user);

    List<Hotel> findByIdIn(List<Long> hotelIds);

    @Query("SELECT DISTINCT h FROM Hotel h " +
            "LEFT JOIN FETCH h.rooms r " +
            "LEFT JOIN FETCH h.hotelReviewSummary " +
            "WHERE h.id IN :hotelIds")
    List<Hotel> findByIdInWithRoomsAndReviewSummary(@Param("hotelIds") List<Long> hotelIds);

    Hotel findByUser(User user);

    List<Hotel> findTop30ByStateOrderByReservationCountDesc(HotelState state);
}
