package com.beyond.HanSoom.common.service;

public record LinkTicketPayload(
        String email,
        String sub,       // OAuth subject
        String provider  // "google" 등
) { }

