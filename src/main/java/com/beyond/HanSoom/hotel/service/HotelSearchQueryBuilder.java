package com.beyond.HanSoom.hotel.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.hotel.dto.HotelListSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
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
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
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
                                .must(m -> m.term(t -> t.field("state.keyword").value("APPLY")))
                        )
                )
                .build();
    }

    // 지역명 오타 대응 검색 쿼리 (Fuzzy Search)
    public Query buildFlexibleAddressQuery(HotelListSearchDto dto) {
        String address = dto.getAddress().trim();

        String queryJson = String.format("""
        {
          "bool": {
            "should": [
              {
                "match": {
                  "address": {
                    "query": "%s",
                    "fuzziness": "AUTO"
                  }
                }
              },
              {
                "wildcard": {
                  "address": "*%s*"
                }
              }
            ],
            "minimum_should_match": 1,
            "filter": [
              {
                "term": {
                  "state": "APPLY"
                }
              },
              {
                "range": {
                  "rooms.maximumPeople": {
                    "gte": %d
                  }
                }
              }
            ]
          }
        }
        """, address, address, dto.getPeople());

        return new StringQuery(queryJson);
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
}