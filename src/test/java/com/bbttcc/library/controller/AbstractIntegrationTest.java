package com.bbttcc.library.controller;

import com.bbttcc.library.entity.Role;
import com.bbttcc.library.entity.User;
import com.bbttcc.library.repository.BookRepository;
import com.bbttcc.library.repository.BorrowRecordRepository;
import com.bbttcc.library.repository.CategoryRepository;
import com.bbttcc.library.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 集成测试基类：启动完整 Spring 上下文 + MockMvc，每个用例前清理数据库。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected BookRepository bookRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected BorrowRecordRepository borrowRecordRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createUser(String username, String rawPassword, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(username)
                .email(username + "@library.local")
                .role(role)
                .enabled(true)
                .build());
    }

    /** 通过登录接口获取访问令牌。 */
    protected String tokenFor(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("username", username);
            put("password", password);
        }});
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("accessToken").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
