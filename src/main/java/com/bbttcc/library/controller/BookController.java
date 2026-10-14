package com.bbttcc.library.controller;

import com.bbttcc.library.dto.ApiResponse;
import com.bbttcc.library.dto.PageResponse;
import com.bbttcc.library.dto.book.BookRequest;
import com.bbttcc.library.dto.book.BookResponse;
import com.bbttcc.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "图书", description = "图书的查询与管理")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "分页查询图书", description = "支持按关键字(书名/作者/ISBN)、分类、是否有库存过滤")
    @GetMapping
    public ApiResponse<PageResponse<BookResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean availableOnly,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ApiResponse.success(PageResponse.of(bookService.search(keyword, categoryId, availableOnly, pageable)));
    }

    @Operation(summary = "查询图书详情")
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(bookService.findById(id));
    }

    @Operation(summary = "新增图书", description = "需要 ADMIN 或 LIBRARIAN 角色")
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("创建成功", bookService.create(request)));
    }

    @Operation(summary = "更新图书", description = "需要 ADMIN 或 LIBRARIAN 角色")
    @PutMapping("/{id}")
    public ApiResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.success("更新成功", bookService.update(id, request));
    }

    @Operation(summary = "删除图书", description = "需要 ADMIN 或 LIBRARIAN 角色；存在未归还记录时不允许删除")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
