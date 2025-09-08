package com.beyond.HanSoom.hotel.domain;

import com.beyond.HanSoom.room.domain.RoomDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "hotels")
public class HotelDocument {

    @Id
    private Long id; // JPA 조회용 ID

    // 검색용 필드들만 최소한으로 유지
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String hotelName;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String addressCity;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String addressDetail;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String address;

    // 자동완성용 필드 - Completion 대신 suggest 필드 활용
    @Field(type = FieldType.Search_As_You_Type, name = "hotel_name_suggest")
    private String hotelNameSuggest;

    @Field(type = FieldType.Search_As_You_Type, name = "address_city_suggest")
    private String addressCitySuggest;

    // 필터링용 필수 필드들
    @Field(type = FieldType.Keyword)
    private String state; // 호텔 상태 (활성/비활성)

    @Field(type = FieldType.Keyword)
    private String type; // 호텔 타입

    // 객실 정보는 필터링에만 필요한 최소 정보만
    @Field(type = FieldType.Nested)
    private List<RoomDocument> rooms;
}