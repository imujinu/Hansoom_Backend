package com.beyond.HanSoom.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommonSuccessDto {
    private Object result;
    private int status_code;
    private String status_message;
}
