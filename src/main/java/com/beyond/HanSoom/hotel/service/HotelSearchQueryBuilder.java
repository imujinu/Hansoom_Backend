package com.beyond.HanSoom.hotel.service;

import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.hotel.dto.HotelListSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HotelSearchQueryBuilder {

    public Query buildImprovedFuzzySearchQuery(HotelListSearchDto dto) {
        log.info("=== 유연한 호텔 검색 쿼리 생성 ===");

        // 호텔명 검색인지 지역명 검색인지 구분
        if (dto.getHotelName() != null && !dto.getHotelName().isBlank()) {
            log.info("호텔명 검색 모드: '{}'", dto.getHotelName());
            return buildHotelNameSearchQuery(dto);
        } else {
            log.info("지역명 검색 모드: '{}'", dto.getAddress());
            return buildAddressSearchQuery(dto);
        }
    }

    // 호텔명 검색 쿼리 (정확한 검색)
    private Query buildHotelNameSearchQuery(HotelListSearchDto dto) {
        String hotelName = dto.getHotelName().trim();
        log.info("호텔명 정확한 검색 쿼리 생성: '{}'", hotelName);
        return buildExactHotelNameQuery(dto, hotelName);
    }

    // 지역명 검색 쿼리 (정확한 검색)
    private Query buildAddressSearchQuery(HotelListSearchDto dto) {
        String address = dto.getAddress().trim();
        log.info("지역명 정확한 검색 쿼리 생성: '{}'", address);
        return buildExactAddressQuery(dto, address);
    }

    // 정확한 호텔명 검색 쿼리
    private Query buildExactHotelNameQuery(HotelListSearchDto dto, String hotelName) {
        log.info("간단한 호텔명 검색: '{}'", hotelName);

        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .bool(innerB -> innerB
                                                .should(s -> s.wildcard(wild -> wild.field("hotelName.keyword").value("*" + hotelName + "*")))
                                                .should(s -> s.match(match -> match.field("hotelName").query(hotelName)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m -> m.term(t -> t.field("state").value("APPLY")))
                        )
                )
                .build();
    }

    // 정확한 지역명 검색 쿼리 (address 통합 컬럼 사용)
    private Query buildExactAddressQuery(HotelListSearchDto dto, String address) {
        log.info("와일드카드 주소 검색: '{}'", address);

        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .bool(innerB -> innerB
                                                // address 필드만 사용 (통합 필드)
                                                .should(s -> s.wildcard(wild -> wild.field("address.keyword").value("*" + address + "*")))

                                                // queryString으로 더 유연한 검색
                                                .should(s -> s.queryString(qs -> qs
                                                        .query("*" + address + "*")
                                                        .defaultField("address")
                                                ))

                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m -> m.term(t -> t.field("state").value("APPLY")))
                        )
                )
                .build();
    }


    // 오타 대응용 - 호텔명 문자 분해 검색 쿼리
    public Query buildFlexibleHotelNameQuery(HotelListSearchDto dto) {
        String hotelName = dto.getHotelName().trim();
        log.info("간단한 오타 대응 호텔명 쿼리: '{}'", hotelName);

        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .bool(innerB -> innerB
                                                // 정확한 검색
                                                .should(s -> s.match(match -> match.field("hotelName").query(hotelName).boost(3.0f)))

                                                // 오타 허용 검색
                                                .should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("hotelName")
                                                        .value(hotelName)
                                                        .fuzziness("AUTO")
                                                        .boost(2.0f)
                                                ))

                                                // 부분 검색
                                                .should(s -> s.wildcard(wild -> wild
                                                        .field("hotelName.keyword")
                                                        .value("*" + hotelName + "*")
                                                        .boost(1.0f)
                                                ))

                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m -> m.term(t -> t.field("state").value("APPLY")))
                        )
                )
                .build();
    }

    // 지역명 오타 대응 검색 쿼리 (Fuzzy Search)
    public Query buildFlexibleAddressQuery(HotelListSearchDto dto) {
        String address = dto.getAddress().trim();
        log.info("지역명 오타 대응 검색: '{}'", address);

    // 입력 주소가 너무 짧으면 정확한 매치만 허용
        boolean isShortAddress = address.length() <= 2;

        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .bool(innerB -> {
                                            // 1. 정확한 매치 (최고 점수)
                                            innerB.should(s -> s.match(match -> match
                                                    .field("address")
                                                    .query(address)
                                                    .boost(10.0f)  // 정확한 매치 점수 높임
                                            ));

                                            // 짧은 주소가 아닐 때만 오타 허용 검색 실행
                                            if (!isShortAddress) {
                                                // 2-1. 첫 글자 일치 + 1개 오타 허용
                                                innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("address")
                                                        .value(address)
                                                        .fuzziness("1")
                                                        .prefixLength(1)  // 첫 글자는 반드시 일치
                                                        .maxExpansions(10)
                                                        .boost(5.0f)
                                                ));

                                                // 2-2. 첫 글자 일치 + 더 관대한 오타 허용 (한글 오타 대응)
                                                innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("address")
                                                        .value(address)
                                                        .fuzziness("AUTO")  // 길이에 따라 자동 조정
                                                        .prefixLength(1)
                                                        .maxExpansions(15)
                                                        .boost(4.5f)
                                                ));

                                                // 3. 2개 오타 허용 (더 엄격한 조건)
                                                if (address.length() >= 5) {  // 5글자 이상일 때만
                                                    innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                            .field("address")
                                                            .value(address)
                                                            .fuzziness("2")
                                                            .prefixLength(2)  // 첫 2글자는 반드시 일치
                                                            .maxExpansions(5)  // 더 제한적
                                                            .boost(3.0f)
                                                    ));
                                                }

                                                // 4. 부분 매치 (더 엄격한 조건)
                                                if (address.length() >= 4) {  // 4글자 이상일 때만
                                                    innerB.should(s -> s.match(match -> match
                                                            .field("address")
                                                            .query(address)
                                                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)  // 모든 단어 포함
                                                            .fuzziness("1")
                                                            .boost(2.0f)
                                                    ));
                                                }
                                            }

                                            return innerB.minimumShouldMatch("1");
                                        })
                                )
                                .must(m -> m.term(t -> t.field("state").value("APPLY")))
                        )
                )
                .build();
    }

    // 타입 필터 추가
    private Query addTypeFilter(Query query, HotelListSearchDto dto) {
        if (dto.getType() != null && !dto.getType().isEmpty()) {
            List<String> typeStrings = dto.getType().stream()
                    .map(HotelType::name)
                    .collect(Collectors.toList());

            // 기존 criteria에 타입 조건 추가
            CriteriaQuery criteriaQuery = (CriteriaQuery) query;
            Criteria newCriteria = criteriaQuery.getCriteria().and("type").in(typeStrings);
            return new CriteriaQuery(newCriteria);
        }
        return query;
    }

//    자동완성 쿼리 생성
    public Query buildAutoCompleteQuery(String query, String searchType, int size) {
        String fieldName = getFieldName(searchType);

        return NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(query)
                                .fields(fieldName, fieldName + "._2gram", fieldName + "._3gram")
                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BoolPrefix)
                        )
                )
                .withFilter(f -> f
                        .term(t -> t
                                .field("state")
                                .value("APPLY")
                        )
                )
                .withPageable(PageRequest.of(0, size))
                .build();
    }

    private String getFieldName(String searchType) {
        return "hotel_name".equals(searchType) ? "hotel_name_suggest" : "address_city_suggest";
    }
}