package com.beyond.HanSoom.wishlist.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.wishlist.dto.WishlistCreateDto;
import com.beyond.HanSoom.wishlist.service.WishlistService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    // Todo - create, list, remove문 개발중
    @PostMapping("/create")
    public ResponseEntity<?> likeHotel(@RequestPart(name = "WishlistCreateDto")WishlistCreateDto dto){
//        wishlistService.likeHotel(dto);
        return new ResponseEntity<>(new CommonSuccessDto("OK",HttpStatus.CREATED.value(), "wishlist is created"), HttpStatus.CREATED);
    }
}
