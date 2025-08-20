package com.beyond.HanSoom.wishlist.service;

import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.beyond.HanSoom.wishlist.domain.Wishlist;
import com.beyond.HanSoom.wishlist.dto.WishlistListDto;
import com.beyond.HanSoom.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    // 찜 추가
    @Transactional
    public void addWishlist(long userId, long hotelId) {
        boolean exists = wishlistRepository.findByUserIdAndHotelId(userId, hotelId).isPresent();
        if (!exists) {
            wishlistRepository.save(Wishlist.builder()
                    .user(userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("사용자 없음")))
                    .hotel(hotelRepository.findById(hotelId)
                            .orElseThrow(() -> new RuntimeException("호텔 없음")))
                    .build());
        }
    }

    // 찜 해제
    @Transactional
    public void removeWishlist(long userId, long hotelId) {
        wishlistRepository.findByUserIdAndHotelId(userId, hotelId)
                .ifPresent(wishlistRepository::delete);
    }

    // 찜 전체조회
    @Transactional(readOnly = true)
    public List<WishlistListDto> getAllWishlist() {
        return wishlistRepository.findAll().stream()
                .map(WishlistListDto::fromEntity)
                .toList();
    }


    // 찜 상세조회
    @Transactional(readOnly = true)
    public List<WishlistListDto> getWishlist(long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistListDto::fromEntity)
                .toList();
    }
}
