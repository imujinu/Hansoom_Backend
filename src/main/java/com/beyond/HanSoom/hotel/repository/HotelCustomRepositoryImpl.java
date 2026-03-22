package com.beyond.HanSoom.hotel.repository;

import com.beyond.HanSoom.hotel.dto.HotelCursorRequestDto;
import com.beyond.HanSoom.hotel.dto.HotelListResponseDto;
import com.beyond.HanSoom.hotel.dto.QHotelListResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.beyond.HanSoom.hotel.domain.QHotel.hotel;
import static com.beyond.HanSoom.review.domain.QHotelReviewSummary.hotelReviewSummary;

@Repository
@RequiredArgsConstructor
public class HotelCustomRepositoryImpl implements HotelCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HotelListResponseDto> searchByCursor(HotelCursorRequestDto requestDto) {
        return queryFactory
                .select(new QHotelListResponseDto(
                        hotel.id,
                        hotel.hotelName,
                        hotel.address,
                        hotel.image,
                        hotel.minPrice,
                        hotel.averageRating,
                        hotelReviewSummary.ratingCount
                ))
                .from(hotel)
                .leftJoin(hotel.hotelReviewSummary, hotelReviewSummary)
                .where(
                        combineCursorCondition(requestDto)
                )
                .orderBy(
                        getSortOrder(requestDto)
                )
                .limit(requestDto.getSize() + 1)
                .fetch();
    }

    private BooleanExpression combineCursorCondition(HotelCursorRequestDto dto) {
        if (dto.getLastId() == null) {
            return null;
        }

        // 문법 오류 수정 (? 추가)
        String sort = (dto.getSort() == null) ? "price_asc" : dto.getSort();

        if (sort.equals("price_asc")) {
            return hotel.minPrice.gt(dto.getLastPrice())
                    .or(hotel.minPrice.eq(dto.getLastPrice()).and(hotel.id.gt(dto.getLastId())));
        } else if (sort.equals("rating_desc")) {
            BigDecimal lastRating = dto.getLastRating();
            return hotel.averageRating.lt(lastRating)
                    .or(hotel.averageRating.eq(lastRating).and(hotel.id.gt(dto.getLastId())));
        }

        return hotel.id.gt(dto.getLastId());
    }

    // 문법 오류 수정 (<?> 제네릭 추가 및 OrderSpecifier import)
    private OrderSpecifier<?>[] getSortOrder(HotelCursorRequestDto dto) {
        String sort = (dto.getSort() == null) ? "price_asc" : dto.getSort();

        if (sort.equals("price_asc")) {
            return new OrderSpecifier[]{
                    hotel.minPrice.asc(),
                    hotel.id.asc()
            };
        } else if (sort.equals("rating_desc")) {
            return new OrderSpecifier[]{
                    hotel.averageRating.desc(),
                    hotel.id.asc()
            };
        }

        return new OrderSpecifier[]{
                hotel.id.asc()
        };
    }
}
