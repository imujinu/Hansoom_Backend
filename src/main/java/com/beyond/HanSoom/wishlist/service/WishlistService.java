package com.beyond.HanSoom.wishlist.service;

import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.beyond.HanSoom.wishlist.domain.Wishlist;
import com.beyond.HanSoom.wishlist.dto.WishlistListDto;
import com.beyond.HanSoom.wishlist.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public void addWishlist(long hotelId) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        Long userId = user.getId();

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
    public void removeWishlist(long hotelId) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        Long userId = user.getId();

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
    public List<WishlistListDto> getWishlist() {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        Long userId = user.getId();

        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistListDto::fromEntity)
                .toList();
    }
}
