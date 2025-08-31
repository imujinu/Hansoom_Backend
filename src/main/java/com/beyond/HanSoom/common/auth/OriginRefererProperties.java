package com.beyond.HanSoom.common.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.csrf")
public class OriginRefererProperties {
    private List<String> allowedOrigins;
    private List<String> skipPaths;
}
