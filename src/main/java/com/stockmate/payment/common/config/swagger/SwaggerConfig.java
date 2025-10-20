package com.stockmate.payment.common.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final String accessTokenHeader = "Authorization";

    private final String refreshTokenHeader = "Authorization-Refresh";

    @Bean
    public OpenAPI openAPI() {
        // Access Token Bearer 인증 스키마 설정
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(accessTokenHeader);

        // Refresh Token Bearer 인증 스키마 설정
        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(refreshTokenHeader);

        // SecurityRequirement 설정 - 각 토큰별 인증 요구사항 추가
        SecurityRequirement accessTokenRequirement = new SecurityRequirement().addList(accessTokenHeader);
        SecurityRequirement refreshTokenRequirement = new SecurityRequirement().addList(refreshTokenHeader);

        // 여러 서버 URL 설정
        Server productionServer = new Server();
        productionServer.setUrl("https://api.stockmate.site");
        productionServer.setDescription("운영 서버");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8004");
        localServer.setDescription("로컬 서버");

        return new OpenAPI()
                .info(new Info()
                        .title("StockMate(결제서버)")
                        .description("StockMate REST API Document")
                        .version("1.2.0"))
                .components(new Components()
                        .addSecuritySchemes(accessTokenHeader, accessTokenScheme)
                        .addSecuritySchemes(refreshTokenHeader, refreshTokenScheme))
                .addServersItem(productionServer)
                .addServersItem(localServer)
                .addSecurityItem(accessTokenRequirement)
                .addSecurityItem(refreshTokenRequirement);
    }

}