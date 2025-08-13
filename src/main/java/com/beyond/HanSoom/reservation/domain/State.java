package com.beyond.HanSoom.reservation.domain;

public enum State {
    PENDING,    // 대기열에서 대기 중
    PROCESSING, // 결제 진행 중
    SUCCEED,    // 결제 성공
    FAILED,       // 결제 실패
    RESERVED,   // 최종 예약 완료
    EXPIRED,    // 시간 초과로 만료
    CANCELLED,   // 사용자 취소
    VALIDATION_FAILED
}
