package com.beyond.HanSoom.reservation.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private String orderId;
    private String status;
    private String message;
    private Long position;
    private String reservationId;

    public static ReservationResponse fail(String message) {
        ReservationResponse response = new ReservationResponse();
        response.status = "FAIL";
        response.message = message;
        return response;
    }

    public static ReservationResponse waiting(Long position) {
        ReservationResponse response = new ReservationResponse();
        response.status = "WAITING";
        response.position = position;
        response.message = "대기 중입니다. 순서: " + position;
        return response;
    }

    public static ReservationResponse success(String reservationId, String orderId) {
        ReservationResponse response = new ReservationResponse();
        response.orderId = orderId;
        response.status = "SUCCESS";
        response.reservationId = reservationId;
        response.message = "예약이 완료되었습니다.";
        return response;
    }

    public static ReservationResponse connect(String reservationId) {
        ReservationResponse response = new ReservationResponse();
        response.status = "connect";
        response.reservationId = reservationId;
        response.message = "실시간 대기열 연결 시작";
        return response;
    }

    // getters, setters...
}