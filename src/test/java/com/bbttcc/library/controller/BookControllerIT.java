package com.bbttcc.library.controller;

import com.bbttcc.library.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 图书接口的集成测试，覆盖公开查询、角色授权与参数校验。
 */
class BookControllerIT extends AbstractIntegrationTest {

    private static final String BOOK_JSON = """
            {"isbn":"9787111544937","title":"深入理解计算机系统","author":"Bryant",
             "publisher":"机械工业出版社","publishYear":2016,"totalCopies":2}
            """;

    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUpUsers() throws Exception {
        createUser("admin", "admin123", Role.ADMIN);
        createUser("alice", "alice123", Role.MEMBER);
        adminToken = tokenFor("admin", "admin123");
        memberToken = tokenFor("alice", "alice123");
    }

    @Test
    @DisplayName("图书查询接口无需登录即可访问")
    void searchIsPublic() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("管理员可新增图书")
    void adminCanCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BOOK_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.availableCopies").value(2));
    }

    @Test
    @DisplayName("普通读者新增图书应返回 403")
    void memberCannotCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BOOK_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ISBN 重复应返回 409")
    void duplicateIsbnReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BOOK_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BOOK_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("缺少必填字段应返回 400")
    void validationReturnsBadRequest() throws Exception {
        String invalid = """
                {"isbn":"9787111544937","author":"Bryant","totalCopies":2}
                """;

        mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.title").exists());
    }

    @Test
    @DisplayName("查询不存在的图书应返回 404")
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
