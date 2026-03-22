package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDto {

    private Long hotelId;
    private Integer rating;
    private String comment;
}
