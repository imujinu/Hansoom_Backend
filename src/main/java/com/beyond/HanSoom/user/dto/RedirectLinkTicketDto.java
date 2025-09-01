package com.beyond.HanSoom.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedirectLinkTicketDto {
    private String linkTicket;
    private boolean rememberMe;
}
