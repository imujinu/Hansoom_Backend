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
    private Query buildExactAddressQuery(HotelListSearchDto dto, String add) {
        log.info("와일드카드 주소 검색: '{}'", add);
        String address = extractSimpleKeyword(add);

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

    // 개선된 주소 검색 쿼리 - 구체적인 검색어 우선
    public Query buildFlexibleAddressQuery(HotelListSearchDto dto) {
        String address = dto.getAddress().trim();
        log.info("지역명 검색: '{}'", address);

        // 주소를 공백으로 분리하여 각 부분 추출
        List<String> addressParts = Arrays.stream(address.split("\\s+"))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toList());

        // 핵심 키워드만 추출 (예: "강원특별자치도" -> "강원")
        String keyword = extractSimpleKeyword(address);
        boolean isShortAddress = address.length() <= 2;
        boolean hasMultipleParts = addressParts.size() > 1;

        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> {
                                    // 여러 부분이 있는 경우 (예: "강원특별자치도 강릉시")
                                    if (hasMultipleParts) {
                                        // 가장 구체적인 부분(마지막 부분)을 필수 조건으로
                                        String mostSpecific = addressParts.get(addressParts.size() - 1);
                                        return m.bool(mustBool -> {
                                            // 마지막 부분(가장 구체적인 지역)은 반드시 포함되어야 함
                                            mustBool.must(specificMust -> specificMust
                                                    .bool(specificBool -> specificBool
                                                            .should(s -> s.wildcard(wildcard -> wildcard
                                                                    .field("address")
                                                                    .value("*" + mostSpecific + "*")
                                                            ))
                                                            .should(s -> s.match(match -> match
                                                                    .field("address")
                                                                    .query(mostSpecific)
                                                                    .fuzziness("1")
                                                            ))
                                                            .minimumShouldMatch("1")
                                                    )
                                            );

                                            // 점수 계산용 should 조건들
                                            mustBool.should(s -> s.bool(scoringBool -> {
                                                // 1. 전체 주소 정확한 매치 (최고 점수)
                                                scoringBool.should(ss -> ss.match(match -> match
                                                        .field("address")
                                                        .query(address)
                                                        .boost(20.0f)
                                                ));

                                                // 2. 전체 주소 부분 매치
                                                scoringBool.should(ss -> ss.wildcard(wildcard -> wildcard
                                                        .field("address")
                                                        .value("*" + address + "*")
                                                        .boost(18.0f)
                                                ));

                                                // 3. 모든 부분이 포함된 경우 (매우 높은 점수)
                                                scoringBool.should(ss -> ss.bool(allPartsBool -> {
                                                    for (String part : addressParts) {
                                                        allPartsBool.must(partMust -> partMust.wildcard(wildcard -> wildcard
                                                                .field("address")
                                                                .value("*" + part + "*")
                                                        ));
                                                    }
                                                    return allPartsBool.boost(15.0f);
                                                }));

                                                // 4. 각 부분별 개별 점수
                                                for (int i = 0; i < addressParts.size(); i++) {
                                                    String part = addressParts.get(i);
                                                    float boost = 10.0f + (addressParts.size() - i); // 뒤쪽(구체적) 부분이 더 높은 점수

                                                    scoringBool.should(ss -> ss.wildcard(wildcard -> wildcard
                                                            .field("address")
                                                            .value("*" + part + "*")
                                                            .boost(boost)
                                                    ));
                                                }

                                                return scoringBool;
                                            }));

                                            return mustBool;
                                        });
                                    } else {
                                        // 단일 부분인 경우 기존 로직 사용
                                        return m.bool(singleBool -> {
                                            // 정확한 매치
                                            singleBool.should(s -> s.match(match -> match
                                                    .field("address")
                                                    .query(address)
                                                    .boost(15.0f)
                                            ));

                                            // 부분 매치
                                            singleBool.should(s -> s.wildcard(wildcard -> wildcard
                                                    .field("address")
                                                    .value("*" + address + "*")
                                                    .boost(12.0f)
                                            ));

                                            // 핵심 키워드 매치
                                            if (!keyword.equals(address)) {
                                                singleBool.should(s -> s.wildcard(wildcard -> wildcard
                                                        .field("address")
                                                        .value("*" + keyword + "*")
                                                        .boost(10.0f)
                                                ));
                                            }

                                            // 오타 허용
                                            if (!isShortAddress) {
                                                singleBool.should(s -> s.fuzzy(fuzzy -> fuzzy
                                                        .field("address")
                                                        .value(address)
                                                        .fuzziness("1")
                                                        .prefixLength(1)
                                                        .boost(8.0f)
                                                ));
                                            }

                                            return singleBool.minimumShouldMatch("1");
                                        });
                                    }
                                })
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

    // 개선된 키워드 추출 함수
    private String extractSimpleKeyword(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        String trimmed = address.trim();

        // 여러 주소 부분이 있는 경우 첫 번째 부분만 사용
        String firstPart = trimmed.split("\\s+")[0];

        // 도/시/구 단위 키워드 추출 규칙
        if (firstPart.endsWith("특별자치도")) {
            return firstPart.replace("특별자치도", "");
        } else if (firstPart.endsWith("특별시")) {
            return firstPart.replace("특별시", "");
        } else if (firstPart.endsWith("광역시")) {
            return firstPart.replace("광역시", "");
        } else if (firstPart.endsWith("도")) {
            return firstPart.replace("도", "");
        } else if (firstPart.endsWith("시")) {
            return firstPart.replace("시", "");
        } else if (firstPart.endsWith("구")) {
            return firstPart.replace("구", "");
        } else if (firstPart.endsWith("군")) {
            return firstPart.replace("군", "");
        }

        // 특별한 규칙이 없으면 원본 반환
        return firstPart;
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