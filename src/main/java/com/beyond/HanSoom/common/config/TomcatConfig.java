package com.beyond.HanSoom.common.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> multipartConfigCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // 톰캣의 최대 파일 개수 제한을 20개로 설정
                connector.setMaxPartCount(30);
            });
        };
    }
}