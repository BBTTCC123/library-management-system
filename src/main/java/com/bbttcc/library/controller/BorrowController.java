package com.bbttcc.library.controller;

import com.bbttcc.library.dto.ApiResponse;
import com.bbttcc.library.dto.PageResponse;
import com.bbttcc.library.dto.borrow.BorrowRequest;
import com.bbttcc.library.dto.borrow.BorrowResponse;
import com.bbttcc.library.entity.BorrowStatus;
import com.bbttcc.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "借阅", description = "借书、还书、续借与借阅记录")
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @Operation(summary = "借书", description = "受最大借阅数量、重复借阅、库存等规则限制")
    @PostMapping
    public ResponseEntity<ApiResponse<BorrowResponse>> borrow(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("借阅成功", borrowService.borrow(request.bookId())));
    }

    @Operation(summary = "还书", description = "读者归还自己的记录；管理员/图书管理员可代还")
    @PostMapping("/{id}/return")
    public ApiResponse<BorrowResponse> returnBook(@PathVariable Long id) {
        return ApiResponse.success("归还成功", borrowService.returnBook(id));
    }

    @Operation(summary = "续借", description = "未逾期且未超过续借次数时可用")
    @PostMapping("/{id}/renew")
    public ApiResponse<BorrowResponse> renew(@PathVariable Long id) {
        return ApiResponse.success("续借成功", borrowService.renew(id));
    }

    @Operation(summary = "我的借阅记录")
    @GetMapping("/me")
    public ApiResponse<PageResponse<BorrowResponse>> myBorrows(
            @RequestParam(required = false) BorrowStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ApiResponse.success(PageResponse.of(borrowService.myBorrows(status, pageable)));
    }

    @Operation(summary = "查询借阅记录详情")
    @GetMapping("/{id}")
    public ApiResponse<BorrowResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(borrowService.findById(id));
    }

    @Operation(summary = "查询全部借阅记录", description = "需要 ADMIN 或 LIBRARIAN 角色")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @GetMapping
    public ApiResponse<PageResponse<BorrowResponse>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) BorrowStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ApiResponse.success(PageResponse.of(borrowService.search(userId, bookId, status, pageable)));
    }
}
