package com.beyond.HanSoom.review.repository;

import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.domain.ReviewState;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 사용자가 작성한 모든 리뷰목록 (User)
    Page<Review> findByUserAndState(Pageable pageable, User user, ReviewState state);

}
