package com.beyond.HanSoom.hotel.dto;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "text") // 이 부분을 추가하세요.
public class AutoCompleteSuggestion {
    private String text;
    private String highlightedText;
}