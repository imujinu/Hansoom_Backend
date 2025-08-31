package com.beyond.HanSoom.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@Component
public class OriginRefererCsrfFilter extends OncePerRequestFilter {

    private final OriginRefererProperties props;

    public OriginRefererCsrfFilter(OriginRefererProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String method = req.getMethod();
        final String uri = req.getRequestURI();

        // 0) 프리플라이트/스킵패스/GET류는 통과
        if (shouldSkip(req)) {
            chain.doFilter(req, res);
            return;
        }

        // 1) 상태 변경 요청만 검사 (POST/PUT/PATCH/DELETE)
        final boolean stateChanging = !(HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method));

        if (!stateChanging) {
            chain.doFilter(req, res);
            return;
        }

        // 2) CSRF는 "쿠키 인증"에서 의미가 있음.
        //    (Bearer 전용 API는 굳이 막지 않도록 쿠키 없으면 패스)
        boolean hasCookie = req.getCookies() != null && req.getCookies().length > 0;
        // refresh 엔드포인트는 쿠키로 인증하는 케이스가 많으니 특별히 검사 대상 포함
        boolean isRefresh = "/user/auth/refresh".equals(uri);

        if (!(hasCookie || isRefresh)) {
            chain.doFilter(req, res);
            return;
        }

        // 3) Origin/Referer에서 오리진 추출
        String source = req.getHeader("Origin");
        if (source == null) {
            String ref = req.getHeader("Referer");
            if (ref != null) {
                try {
                    URI u = URI.create(ref);
                    String hostPort = (u.getPort() == -1 ? u.getHost() : (u.getHost() + ":" + u.getPort()));
                    source = u.getScheme() + "://" + hostPort;
                } catch (IllegalArgumentException ignored) {
                    source = null;
                }
            }
        }

        // 4) 허용 오리진 검증 (정확 일치)
        if (source == null || props.getAllowedOrigins() == null
                || !props.getAllowedOrigins().contains(source)) {
            // 여기서 바로 403 JSON 반환하고 체인 종료 (예외 던지지 않음)
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.setHeader("X-CSRF-Reason", "INVALID_ORIGIN");
            res.getWriter().write("{\"status\":403,\"message\":\"CSRF: invalid origin/referer\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean shouldSkip(HttpServletRequest req) {
        String uri = req.getRequestURI();

        // OPTIONS(프리플라이트) 무조건 통과
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;

        // yml skip-paths
        if (props.getSkipPaths() != null) {
            for (String p : props.getSkipPaths()) {
                if (uri.startsWith(p)) return true;
            }
        }
        return false;
    }
}
