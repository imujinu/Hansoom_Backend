package com.beyond.HanSoom.review.repository;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.review.domain.HotelReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelReviewSummaryRepository extends JpaRepository<HotelReviewSummary, Long> {
    HotelReviewSummary findByHotel(Hotel hotel);
    HotelReviewSummary findByHotelId(Long hotelId);

}
