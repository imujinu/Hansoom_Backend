package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AutoCompleteSuggestion {
    private String text;
    private String highlightedText;
}