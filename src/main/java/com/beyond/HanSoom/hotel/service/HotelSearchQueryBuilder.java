package com.beyond.HanSoom.hotel.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
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

import java.util.Arrays;
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
                                                .should(s -> s.wildcard(wild -> wild.field("hotelName").value("*" + hotelName + "*")))
                                                .should(s -> s.match(match -> match.field("hotelName").query(hotelName)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
                                .filter(f -> {
                                    if (dto.getType() != null && !dto.getType().isEmpty()) {
                                        return f.terms(terms -> terms
                                                .field("type.keyword")
                                                .terms(t -> t.value(dto.getType().stream()
                                                        .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                                                        .collect(Collectors.toList()))));
                                    }
                                    return f.matchAll(ma -> ma);
                                })
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
                                .must(m -> m.matchPhrase(mp -> mp.field("address").query(address)))
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
                                .filter(f -> {
                                    if (dto.getType() != null && !dto.getType().isEmpty()) {
                                        return f.terms(terms -> terms
                                                .field("type.keyword")
                                                .terms(t -> t.value(dto.getType().stream()
                                                        .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                                                        .collect(Collectors.toList()))));
                                    }
                                    return f.matchAll(ma -> ma);
                                })
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
                                                // 1. 정확한 검색 (최고 점수)
                                                .should(s -> s.match(match -> match
                                                        .field("hotelName")
                                                        .query(hotelName)
                                                        .boost(5.0f)
                                                ))

                                                // 2. 부분 검색 (wildcard)
                                                .should(s -> s.wildcard(wild -> wild
                                                        .field("hotelName")
                                                        .value("*" + hotelName + "*")
                                                        .boost(4.0f)
                                                ))

                                                // 3. 엄격한 오타 허용 (1글자만)
                                                .should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("hotelName")
                                                        .value(hotelName)
                                                        .fuzziness("1") // 1글자만 허용
                                                        .prefixLength(1) // 첫글자 일치 필수
                                                        .maxExpansions(5) // 확장 제한
                                                        .boost(2.0f)
                                                ))

                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
                                .filter(f -> {
                                    if (dto.getType() != null && !dto.getType().isEmpty()) {
                                        return f.terms(terms -> terms
                                                .field("type.keyword")
                                                .terms(t -> t.value(dto.getType().stream()
                                                        .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                                                        .collect(Collectors.toList()))));
                                    }
                                    return f.matchAll(ma -> ma);
                                })
                        )
                )
                .build();
    }

    // 간단한 주소 검색 쿼리
    public Query buildFlexibleAddressQuery(HotelListSearchDto dto) {
        String address = dto.getAddress().trim();
        log.info("지역명 검색: '{}'", address);

        // 핵심 키워드만 추출 (예: "강원특별자치도" -> "강원")
        String keyword = extractSimpleKeyword(address);
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
                                                    .boost(15.0f)
                                            ));

                                            // 2. 원본 주소로 부분 매치
                                            innerB.should(s -> s.wildcard(wildcard -> wildcard
                                                    .field("address")
                                                    .value("*" + address + "*")
                                                    .boost(12.0f)
                                            ));

                                            // 3. 핵심 키워드로 부분 매치
                                            if (!keyword.equals(address)) {
                                                innerB.should(s -> s.wildcard(wildcard -> wildcard
                                                        .field("address")
                                                        .value("*" + keyword + "*")
                                                        .boost(10.0f)
                                                ));
                                            }

                                            // === 오타 검증 로직 시작 ===
                                            if (!isShortAddress) {
                                                // 4. 1글자 오타 허용 (첫 글자 일치 필수)
                                                innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("address")
                                                        .value(address)
                                                        .fuzziness("1")
                                                        .prefixLength(1)  // 첫 글자는 반드시 일치
                                                        .maxExpansions(10)
                                                        .boost(8.0f)
                                                ));

                                                // 5. AUTO 오타 허용 (길이에 따라 자동 조정)
                                                innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("address")
                                                        .value(address)
                                                        .fuzziness("AUTO")
                                                        .prefixLength(1)
                                                        .boost(7.0f)
                                                ));

                                                // 6. 키워드에 대한 오타 허용
                                                if (!keyword.equals(address) && keyword.length() > 2) {
                                                    innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                            .field("address")
                                                            .value(keyword)
                                                            .fuzziness("1")
                                                            .prefixLength(1)
                                                            .boost(6.0f)
                                                    ));
                                                }

                                                // 7. 더 관대한 오타 허용 (5글자 이상일 때)
                                                if (address.length() >= 5) {
                                                    innerB.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                            .field("address")
                                                            .value(address)
                                                            .fuzziness("2")
                                                            .prefixLength(2)  // 첫 2글자는 일치
                                                            .maxExpansions(5)
                                                            .boost(5.0f)
                                                    ));
                                                }

                                                // 8. Match query with fuzziness (단어 단위 오타)
                                                innerB.should(s -> s.match(match -> match
                                                        .field("address")
                                                        .query(address)
                                                        .fuzziness("1")
                                                        .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                                                        .boost(4.0f)
                                                ));
                                            }
                                            // === 오타 검증 로직 끝 ===

                                            return innerB.minimumShouldMatch("1");
                                        })
                                )
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
                                .filter(f -> {
                                    if (dto.getType() != null && !dto.getType().isEmpty()) {
                                        return f.terms(terms -> terms
                                                .field("type.keyword")
                                                .terms(t -> t.value(dto.getType().stream()
                                                        .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                                                        .collect(Collectors.toList()))));
                                    }
                                    return f.matchAll(ma -> ma);
                                })
                        )
                )
                .build();
    }

    // 간단한 키워드 추출 메서드
    private String extractSimpleKeyword(String address) {
        // "강원특별자치도" -> "강원", "서울특별시" -> "서울" 등
        return address.replaceAll("(특별자치도|특별자치시|특별시|광역시|도|시|군|구).*", "").trim();
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
                        .matchPhrasePrefix(mp -> mp
                                .field(fieldName)
                                .query(query)
                                .maxExpansions(10)
                        )
                )
                .withFilter(f -> f
                        .term(t -> t
                                .field("state.keyword")
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