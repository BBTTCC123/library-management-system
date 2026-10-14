package com.bbttcc.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 文档配置，注册 JWT Bearer 鉴权方案。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("图书借阅管理系统 API")
                        .version("1.0.0")
                        .description("""
                                基于 Spring Boot 3 + Spring Security(JWT) + Spring Data JPA 的图书借阅管理系统。

                                使用步骤：
                                1. 调用 `/api/auth/login` 获取 `accessToken`；
                                2. 点击右上角 Authorize，填入该 token；
                                3. 即可调用需要登录的接口。

                                内置账号：admin/admin123（管理员）、librarian/librarian123（图书管理员）、alice/alice123（读者）。
                                """)
                        .contact(new Contact().name("BBTTCC").url("https://github.com/BBTTCC123"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("在此填入登录接口返回的 accessToken")));
    }
}
