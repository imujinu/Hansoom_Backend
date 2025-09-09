package com.beyond.HanSoom.common.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public ResponseCookie buildRefreshTokenCookie(
            String name,
            String value,
            String domain,
            String path,
            String sameSite,      // "None" | "Lax" | "Strict"
            boolean secure,
            boolean httpOnly,
            long maxAgeDays
    ) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .path(path)
                .maxAge(maxAgeDays * 24 * 60 * 60);

        if (domain != null && !domain.isBlank()) {
            b.domain(domain);
        }
        // 스프링 6/부트 3에서 sameSite 커스텀은 문자열로
        if (sameSite != null && !sameSite.isBlank()) {
            b.sameSite(sameSite);
        }
        return b.build();
    }

    public ResponseCookie deleteCookie(
            String name, String domain, String path, String sameSite, boolean secure, boolean httpOnly
    ) {
        return buildRefreshTokenCookie(name, "", domain, path, sameSite, secure, httpOnly, 0);
    }
}
