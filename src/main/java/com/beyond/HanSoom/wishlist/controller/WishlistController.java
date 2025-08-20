package com.beyond.HanSoom.wishlist.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.wishlist.dto.WishlistLikeDto;
import com.beyond.HanSoom.wishlist.dto.WishlistListDto;
import com.beyond.HanSoom.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/like")
    public ResponseEntity<?> like(@RequestBody WishlistLikeDto dto){
        wishlistService.addWishlist(dto.getUserId(), dto.getHotelId());
        return new ResponseEntity<>(new CommonSuccessDto("OK",HttpStatus.CREATED.value(), "wishlist is added"), HttpStatus.CREATED);
    }

    @DeleteMapping("/dislike")
    public ResponseEntity<?> dislike(@RequestBody WishlistLikeDto dto){
        wishlistService.removeWishlist(dto.getUserId(), dto.getHotelId());
        return new ResponseEntity<>(new CommonSuccessDto("OK",HttpStatus.CREATED.value(), "wishlist is removed"), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllWishList(){
        List<WishlistListDto> wishlist = wishlistService.getAllWishlist();
        if (wishlist.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getUserWishlist(@PathVariable Integer userId){
        List<WishlistListDto> wishlist = wishlistService.getWishlist(userId);
        if (wishlist.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(new CommonSuccessDto("OK",HttpStatus.CREATED.value(), "wishlist"), HttpStatus.CREATED);
    }
}
