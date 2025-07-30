package com.beyond.HanSoom.hotel.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GeocoderService {

    @Value("${cloud.kakao.rest-api-key}")
    private String kakaoApiKey;

    private static final String KAKAO_GEOCODE_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    public Coordinate getCoordinates(String address) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(KAKAO_GEOCODE_URL)
                .queryParam("query", address);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        JsonNode documents = response.getBody().get("documents");
        if (documents != null && documents.size() > 0) {
            JsonNode location = documents.get(0);
            double latitude = Double.parseDouble(location.get("y").asText());
            double longitude = Double.parseDouble(location.get("x").asText());
            return new Coordinate(latitude, longitude);
        }

        throw new RuntimeException("주소로 좌표를 찾을 수 없습니다: " + address);
    }

    @Getter
    @AllArgsConstructor
    public static class Coordinate {
        private double latitude;
        private double longitude;
    }
}