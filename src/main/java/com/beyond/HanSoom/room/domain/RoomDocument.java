package com.beyond.HanSoom.room.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDocument {
    private Long id;
    @Field(type = FieldType.Integer)
    private int maximumPeople;
    @Field(type = FieldType.Keyword)
    private String state;
}
