package com.bbttcc.library.controller;

import com.bbttcc.library.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证相关接口的集成测试。
 */
class AuthControllerIT extends AbstractIntegrationTest {

    private static final String REGISTER = """
            {"username":"charlie","password":"charlie123","fullName":"Charlie","email":"charlie@library.local"}
            """;

    @Test
    @DisplayName("注册成功应返回 201 与访问令牌")
    void registerSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("charlie"))
                .andExpect(jsonPath("$.data.user.role").value("MEMBER"));
    }

    @Test
    @DisplayName("用户名重复注册应返回 409")
    void registerDuplicateUsername() throws Exception {
        createUser("charlie", "charlie123", Role.MEMBER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("参数校验失败应返回 400 与字段错误")
    void registerValidationFails() throws Exception {
        String invalid = """
                {"username":"ab","password":"123","fullName":"","email":"not-an-email"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.password").exists())
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("正确凭证登录应返回令牌")
    void loginSuccess() throws Exception {
        createUser("dave", "dave123", Role.MEMBER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dave\",\"password\":\"dave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("密码错误应返回 401")
    void loginWrongPassword() throws Exception {
        createUser("dave", "dave123", Role.MEMBER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dave\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("未携带令牌访问受保护接口应返回 401")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("携带有效令牌可访问当前用户接口")
    void currentUserWithToken() throws Exception {
        createUser("erin", "erin123", Role.MEMBER);
        String token = tokenFor("erin", "erin123");

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("erin"));
    }

    @Test
    @DisplayName("普通用户访问用户管理接口应返回 403")
    void memberCannotAccessUserManagement() throws Exception {
        createUser("frank", "frank123", Role.MEMBER);
        String token = tokenFor("frank", "frank123");

        mockMvc.perform(get("/api/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }
}
