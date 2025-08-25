package com.beyond.HanSoom.wishlist.dto;

import com.beyond.HanSoom.wishlist.domain.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class WishlistListDto {
    private long id;
    private long hotelId;
    private String hotelName;

    public static WishlistListDto fromEntity(Wishlist wishlist){
        return WishlistListDto.builder()
                .id(wishlist.getId())
                .hotelId(wishlist.getHotel().getId())
                .hotelName(wishlist.getHotel().getHotelName())
                .build();
    }
}
