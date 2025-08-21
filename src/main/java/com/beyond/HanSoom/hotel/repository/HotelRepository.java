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

    Optional<Hotel> findByIdAndState(Long id, HotelState hotelState);


    @Query(value = """
    SELECT h.*, (6371 * acos(
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

    Page<Hotel> findByState(Pageable pageable, HotelState hotelState);

    List<Hotel> findAllByUser(User host);
}
