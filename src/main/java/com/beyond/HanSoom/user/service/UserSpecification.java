package com.beyond.HanSoom.user.service;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.dto.UserSearchDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> search(UserSearchDto dto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
//                predicateList.add(criteriaBuilder.equal(root.get("delYn"), "N")); // Todo - 호텔 id, role 등으로 필터링
            if(dto.getName() != null) {
                predicateList.add(criteriaBuilder.like(root.get("name"), "%"+dto.getName()+"%"));
            }

            Predicate[] predicateArr = new Predicate[predicateList.size()];
            for(int i = 0 ; i < predicateList.size() ; i++) {
                predicateArr[i] = predicateList.get(i);
            }
            // 위의 검색 조건들을 하나(한줄)의 Predicate 객체로 만들어서 return
            Predicate predicate = criteriaBuilder.and(predicateArr);

            return predicate;
        };
    }
}

