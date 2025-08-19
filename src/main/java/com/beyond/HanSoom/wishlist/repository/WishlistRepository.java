package com.beyond.HanSoom.wishlist.repository;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.wishlist.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 조인한 User Entity를 통해 유저마다 개개인의 찜을 찾기 위해서 repository에 findByUser 메서드를 추가하였다.
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    List<Wishlist> findByUser(User user);
}
