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
     * 주어진 주소를 위도/경도와 행정구역 정보로 변환하여 반환
     *
     * @param fullAddress 정확한 도로명 주소 또는 지번 주소
     * @return HotelAddressDto (addressCity, addressDetail, latitude, longitude)
     * @throws RuntimeException 주소 검색 실패 시
     */
    public HotelAddressDto parseAddress(String fullAddress) {
        WebClient webClient = WebClient.builder()
                .baseUrl(KAKAO_GEOCODE_URL)
                .build();

        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", fullAddress)
                            .build())
                    .header("Authorization", "KakaoAK " + kakaoApiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.has("documents")) {
                throw new RuntimeException("카카오 응답이 비정상입니다: " + fullAddress);
            }

            JsonNode documents = response.get("documents");
            if (!documents.isArray() || documents.isEmpty()) {
                throw new IllegalArgumentException("해당 주소로 좌표를 찾을 수 없습니다: " + fullAddress);
            }

            JsonNode firstDocument = documents.get(0);
            JsonNode addressInfo = firstDocument.get("address");

            // 좌표 추출
            double longitude = Double.parseDouble(firstDocument.get("x").asText());
            double latitude = Double.parseDouble(firstDocument.get("y").asText());

            // 행정구역 정보 추출
            String region1 = addressInfo.get("region_1depth_name").asText(); // 시/도
            String region2 = addressInfo.get("region_2depth_name").asText(); // 시/군/구
            String region3 = addressInfo.get("region_3depth_name").asText(); // 구/읍/면/동

            // addressCity와 addressDetail 분리 (개선된 로직)
            String addressCity;
            String addressDetail;

            if (region1.endsWith("특별시") || region1.endsWith("광역시")) {
                // 서울, 부산 등: 시 + 구
                addressCity = region1 + " " + region2;
                addressDetail = extractDetailAddress(fullAddress, addressCity, region2);
            } else if (!region3.isEmpty() && region2.endsWith("시") && region3.endsWith("구")) {
                // 수원시 영통구, 성남시 분당구 등: 도 + 시 + 구
                addressCity = region1 + " " + region2 + " " + region3;
                addressDetail = extractDetailAddress(fullAddress, addressCity, region3);
            } else {
                // 일반적인 경우: 도 + 시
                addressCity = region1 + " " + region2;
                addressDetail = extractDetailAddress(fullAddress, addressCity, region2);
            }

            log.info("[HANSOOM][INFO]주소 파싱 결과 - 원본: {}, City: {}, Detail: {}",
                    fullAddress, addressCity, addressDetail);

            return new HotelAddressDto(addressCity, addressDetail, latitude, longitude);

        } catch(IllegalArgumentException e) {
            log.error("[HANSOOM][ERROR] - 카카오 지오코딩 API 호출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("카카오 지오코딩 API 호출 실패: " + e.getMessage());
        } catch (WebClientResponseException e) {
            log.error("[HANSOOM][ERROR] - WebClient HTTP 에러 발생:", e);
            throw new RuntimeException("카카오 지오코딩 API 호출 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[HANSOOM][ERROR] - WebClient 일반 에러 발생: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 지오코딩 API 호출 중 예외 발생: " + e.getMessage());
        }
    }

    /**
     * 전체 주소에서 도시 부분을 제거하고 상세 주소만 추출
     */
    private String extractDetailAddress(String fullAddress, String addressCity, String lastRegion) {
        log.debug("주소 분리 시작 - 전체주소: {}, 도시: {}", fullAddress, addressCity);

        String remaining = fullAddress;

        if (addressCity != null && !addressCity.isEmpty()) {
            // addressCity의 마지막 부분 추출
            String[] cityParts = addressCity.split("\\s+");
            String lastCityPart = cityParts[cityParts.length - 1]; // "강릉시"

            log.debug("마지막 도시 부분: {}", lastCityPart);

            // fullAddress에서 마지막 도시 부분의 인덱스를 찾음
            int lastIndex = remaining.lastIndexOf(lastCityPart);

            if (lastIndex != -1) {
                // 해당 부분 이후의 문자열 추출
                remaining = remaining.substring(lastIndex + lastCityPart.length()).trim();
                log.debug("'{}' 이후 부분 추출: {}", lastCityPart, remaining);
            }
        }

        // 연속된 공백 정리
        remaining = remaining.replaceAll("\\s+", " ").trim();

        log.debug("최종 결과: {}", remaining);

        return remaining.isEmpty() ? fullAddress : remaining;
    }

    @Getter
    @AllArgsConstructor
    public static class HotelAddressDto {
        private String addressCity;
        private String addressDetail;
        private double latitude;
        private double longitude;
    }
}