package com.bbttcc.library.controller;

import com.bbttcc.library.entity.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 借阅全流程集成测试：创建图书 -> 借书 -> 重复借书 -> 查询 -> 归还 -> 库存回补。
 */
class BorrowFlowIT extends AbstractIntegrationTest {

    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUpUsers() throws Exception {
        createUser("admin", "admin123", Role.ADMIN);
        createUser("alice", "alice123", Role.MEMBER);
        adminToken = tokenFor("admin", "admin123");
        memberToken = tokenFor("alice", "alice123");
    }

    private long createBook(String isbn, String title, int copies) throws Exception {
        String body = """
                {"isbn":"%s","title":"%s","author":"Author","totalCopies":%d}
                """.formatted(isbn, title, copies);
        MvcResult result = mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("data").path("id").asLong();
    }

    @Test
    @DisplayName("借书 -> 归还全流程，库存应正确增减")
    void borrowThenReturnFlow() throws Exception {
        long bookId = createBook("9787111111111", "测试图书A", 1);

        MvcResult borrow = mockMvc.perform(post("/api/borrows")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("BORROWED"))
                .andExpect(jsonPath("$.data.renewCount").value(0))
                .andReturn();
        long recordId = objectMapper.readTree(borrow.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 库存已扣减为 0
        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(jsonPath("$.data.availableCopies").value(0));

        // 重复借阅同一本书 -> 400
        mockMvc.perform(post("/api/borrows")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isBadRequest());

        // 我的借阅记录
        mockMvc.perform(get("/api/borrows/me").header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // 归还
        mockMvc.perform(post("/api/borrows/" + recordId + "/return")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"))
                .andExpect(jsonPath("$.data.fineAmount").value(0.0));

        // 库存已回补
        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(jsonPath("$.data.availableCopies").value(1));
    }

    @Test
    @DisplayName("库存为 0 时借书应返回 400")
    void borrowOutOfStock() throws Exception {
        long bookId = createBook("9787222222222", "测试图书B", 1);

        // 先用第一个读者借走唯一一本
        String secondToken;
        createUser("bob", "bob123", Role.MEMBER);
        secondToken = tokenFor("bob", "bob123");

        mockMvc.perform(post("/api/borrows")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/borrows")
                        .header("Authorization", bearer(secondToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("该图书暂无可借库存"));
    }

    @Test
    @DisplayName("超过最大借阅数量（测试环境为 3）应返回 400")
    void borrowBeyondLimit() throws Exception {
        for (int i = 0; i < 4; i++) {
            long bookId = createBook("97873333333" + i, "测试图书" + i, 1);
            if (i < 3) {
                mockMvc.perform(post("/api/borrows")
                                .header("Authorization", bearer(memberToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"bookId\":" + bookId + "}"))
                        .andExpect(status().isCreated());
            } else {
                mockMvc.perform(post("/api/borrows")
                                .header("Authorization", bearer(memberToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"bookId\":" + bookId + "}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value(
                                org.hamcrest.Matchers.containsString("最大借阅数量")));
            }
        }
    }

    @Test
    @DisplayName("续借成功后到期日应延长且续借次数 +1")
    void renewExtendsDueDate() throws Exception {
        long bookId = createBook("9787444444444", "测试图书C", 1);

        MvcResult borrow = mockMvc.perform(post("/api/borrows")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        long recordId = objectMapper.readTree(borrow.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/borrows/" + recordId + "/renew")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.renewCount").value(1));

        // 第二次续借应被拒绝（测试环境 max-renew-count=1）
        mockMvc.perform(post("/api/borrows/" + recordId + "/renew")
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("读者不能归还他人的借阅记录")
    void cannotReturnOthersRecord() throws Exception {
        long bookId = createBook("9787555555555", "测试图书D", 1);
        createUser("bob", "bob123", Role.MEMBER);
        String bobToken = tokenFor("bob", "bob123");

        MvcResult borrow = mockMvc.perform(post("/api/borrows")
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        long recordId = objectMapper.readTree(borrow.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/borrows/" + recordId + "/return")
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isForbidden());
    }
}
