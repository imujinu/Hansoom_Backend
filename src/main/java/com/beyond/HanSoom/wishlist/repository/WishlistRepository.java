package com.beyond.HanSoom.wishlist.repository;

import com.beyond.HanSoom.wishlist.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Optional<Wishlist> findByUserIdAndHotelId(long userId, long hotelId);

    List<Wishlist> findByUserId(long userId);
}
