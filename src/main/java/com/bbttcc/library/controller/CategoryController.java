package com.bbttcc.library.controller;

import com.bbttcc.library.dto.ApiResponse;
import com.bbttcc.library.dto.category.CategoryRequest;
import com.bbttcc.library.dto.category.CategoryResponse;
import com.bbttcc.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "分类", description = "图书分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "查询全部分类")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> findAll() {
        return ApiResponse.success(categoryService.findAll());
    }

    @Operation(summary = "查询分类详情")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.findById(id));
    }

    @Operation(summary = "新增分类", description = "需要 ADMIN 或 LIBRARIAN 角色")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("创建成功", categoryService.create(request)));
    }

    @Operation(summary = "更新分类", description = "需要 ADMIN 或 LIBRARIAN 角色")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success("更新成功", categoryService.update(id, request));
    }

    @Operation(summary = "删除分类", description = "需要 ADMIN 或 LIBRARIAN 角色；分类下存在图书时不允许删除")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
