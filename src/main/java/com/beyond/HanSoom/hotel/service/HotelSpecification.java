package com.beyond.HanSoom.hotel.service;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.hotel.dto.HotelListSearchDto;
import com.beyond.HanSoom.hotel.dto.HotelPopularRequestDto;
import com.beyond.HanSoom.room.domain.Room;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecification {
    public static Specification<Hotel> withSearchConditions(HotelListSearchDto dto) {
        return (root, query, cb) -> {
            query.distinct(true); // 중복 제거
            Join<Hotel, Room> roomJoin = root.join("rooms", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("state"), HotelState.APPLY));
            predicates.add(cb.notEqual(roomJoin.get("state"), HotelState.REMOVE));
            predicates.add(cb.greaterThanOrEqualTo(roomJoin.get("maximumPeople"), dto.getPeople()));

            if (dto.getHotelName() != null && !dto.getHotelName().isBlank()) {
                predicates.add(cb.like(root.get("hotelName"), "%" + dto.getHotelName() + "%"));
            } else if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
                predicates.add(cb.like(root.get("address"), "%" + dto.getAddress() + "%"));
            }
            if (!dto.getType().isEmpty()) {
                predicates.add(root.get("type").in(dto.getType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Hotel> withSearchConditionsPop(HotelPopularRequestDto dto) {
        return (root, query, cb) -> {
            query.distinct(true); // 중복 제거
            Join<Hotel, Room> roomJoin = root.join("rooms", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("state"), HotelState.APPLY));
            predicates.add(cb.notEqual(roomJoin.get("state"), HotelState.REMOVE));
            predicates.add(cb.greaterThanOrEqualTo(roomJoin.get("maximumPeople"), dto.getPeople()));

            if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
                predicates.add(cb.like(root.get("address"), "%" + dto.getAddress() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}