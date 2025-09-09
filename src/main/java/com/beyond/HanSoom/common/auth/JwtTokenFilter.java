package com.beyond.HanSoom.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JwtTokenFilter extends GenericFilter {
    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            String bearerToken = req.getHeader("Authorization");
            if(bearerToken == null) {
                // token이 없는 경우 다시 filterchain으로 되돌아가는 로직
                chain.doFilter(request, response);
                return;
            }

            // token이 있는 경우 토큰 검증 후 Authentication 객체 생성
            String token = bearerToken.substring(7);
            // token 검증 및 claims 추출
            // 아래 코드 자체가 검증하는 코드 (문제 있으면 오류 터짐) // 얘는 500
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            List<GrantedAuthority> authorityList = new ArrayList<>();
            // authentication객체를 만들 때 권한은 ROLE_ 이라는 키워드를 붙여서 만들어 주는 것이 추후 문제 발생 X
            authorityList.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorityList);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 401, 403은 따로 잡고 있고, 500은 그냥 출력
            log.error("[HANSOOM][ERROR] - JwtTokenFilter/doFilter/Exception - {}", e.getMessage());
        }
        chain.doFilter(request, response);
    }
}
