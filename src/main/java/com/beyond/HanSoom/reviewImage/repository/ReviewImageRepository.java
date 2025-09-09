package com.beyond.HanSoom.reviewImage.repository;

import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    @Query("""
        select ri
        from ReviewImage ri
        join ri.review r
        where r.state = com.beyond.HanSoom.review.domain.ReviewState.NORMAL
          and r.hotel.id = :hotelId
    """)
    List<ReviewImage> findImagesByHotelIdAndReviewNormal(@Param("hotelId") Long hotelId);

}
