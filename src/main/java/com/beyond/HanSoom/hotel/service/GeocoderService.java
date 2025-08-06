package com.beyond.HanSoom.hotel.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocoderService {

    @Value("${cloud.kakao.rest-api-key}")
    private String kakaoApiKey;

    private static final String KAKAO_GEOCODE_URL = "https://dapi.kakao.com";
    /**
     * 주어진 주소를 위도/경도로 변환하여 반환
     *
     * @param address 정확한 도로명 주소
     * @return Coordinate(latitude, longitude)
     * @throws RuntimeException 주소 검색 실패 시
     */
    public Coordinate getCoordinates(String address) {
        WebClient webClient = WebClient.builder()
                .baseUrl(KAKAO_GEOCODE_URL)
                .build();

        log.info("[HANSOOM][INFO] - 카카오 API Key: {}", kakaoApiKey);
        log.info("[HANSOOM][INFO] - 요청 주소: {}", address);

        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .header("Authorization", "KakaoAK " + kakaoApiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))  // 10초 타임아웃
                    .block();

            log.info("[HANSOOM][INFO] - 카카오 지오코더 응답: {}", response);

            // 응답 검증
            if (response == null || !response.has("documents")) {
                throw new RuntimeException("카카오 응답이 비정상입니다: " + address);
            }

            JsonNode documents = response.get("documents");
            if (!documents.isArray() || documents.size() == 0) {
                throw new RuntimeException("주소로 좌표를 찾을 수 없습니다: " + address);
            }

            JsonNode first = documents.get(0);
            if (!first.has("x") || !first.has("y")) {
                throw new RuntimeException("카카오 응답에 좌표 정보가 없습니다: " + address);
            }

            // 좌표 추출
            double longitude = Double.parseDouble(first.get("x").asText());
            double latitude = Double.parseDouble(first.get("y").asText());

            log.info("[HANSOOM][INFO] - 변환된 좌표 - 위도: {}, 경도: {}", latitude, longitude);

            return new Coordinate(latitude, longitude);

        } catch (WebClientResponseException e) {
            log.error("[HANSOOM][ERROR] - WebClient HTTP 에러 발생:");
            log.error("[HANSOOM][ERROR] - Status: {}", e.getStatusCode());
            log.error("[HANSOOM][ERROR] - Response Body: {}", e.getResponseBodyAsString());
            log.error("[HANSOOM][ERROR] - Request Headers가 포함된 에러: {}", e.getMessage());
            throw new RuntimeException("카카오 지오코딩 API 호출 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[HANSOOM][ERROR] - WebClient 일반 에러 발생: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 지오코딩 API 호출 중 예외 발생: " + e.getMessage());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Coordinate {
        private double latitude;   // 위도
        private double longitude;  // 경도
    }
}
