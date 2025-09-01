package com.beyond.HanSoom.common.auth;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    @Value("${jwt.accessTokenExpiryMinutes}")
    private int accessTokenExpiryMinutes;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.refreshTokenExpiryDaysNonPersistent}")
    private int refreshTokenExpiryDaysNonPersistent;
    @Value("${jwt.refreshTokenExpiryDaysPersistent}")
    private int refreshTokenExpiryDaysPersistent;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    private Key secret_rt_key;

    @Autowired
    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512.getJcaName());
        secret_rt_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(User user) {
        String email = user.getEmail();
        Long userId = user.getId();
        String role = user.getUserRole().toString();

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("userId", userId);
        Date now = new Date();
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpiryMinutes*60*1000L))
                .signWith(secret_at_key)
                .compact();

        return accessToken;
    }

    public String createRtToken(User user, boolean rememberMe) {
        String email = user.getEmail();
        String role = user.getUserRole().toString();
        Long userId = user.getId();;
        int refreshTokenExpiryDays = rememberMe ? refreshTokenExpiryDaysPersistent : refreshTokenExpiryDaysNonPersistent;
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("userId", userId);
        Date now = new Date();
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpiryDays * 24 * 60 * 60 * 1000L))
                .signWith(secret_rt_key)
                .compact();

        // rt 토큰을 redis에 저장
        redisTemplate.opsForValue().set(user.getEmail(), refreshToken, refreshTokenExpiryDays, TimeUnit.DAYS);

        return refreshToken;
    }

    public User validateRt(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret_rt_key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String email = claims.getSubject();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        // redis의 값과 비교하는 검증
        String redisRt = redisTemplate.opsForValue().get(user.getEmail());
        if(!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }

        return user;
    }

    public String removeRt(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret_rt_key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String email = claims.getSubject();
        userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        redisTemplate.delete(email);

        return email;
    }
}
