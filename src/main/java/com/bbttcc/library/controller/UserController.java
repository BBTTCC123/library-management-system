package com.bbttcc.library.controller;

import com.bbttcc.library.dto.ApiResponse;
import com.bbttcc.library.dto.PageResponse;
import com.bbttcc.library.dto.user.UpdateRoleRequest;
import com.bbttcc.library.dto.user.UserResponse;
import com.bbttcc.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户", description = "当前用户信息与用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(userService.currentUser());
    }

    @Operation(summary = "分页查询用户", description = "需要 ADMIN 角色")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> findAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ApiResponse.success(PageResponse.of(userService.findAll(pageable)));
    }

    @Operation(summary = "查询用户详情", description = "需要 ADMIN 角色")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(userService.findById(id));
    }

    @Operation(summary = "修改用户角色", description = "需要 ADMIN 角色")
    @PatchMapping("/{id}/role")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id,
                                                @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.success("角色更新成功", userService.updateRole(id, request.role()));
    }

    @Operation(summary = "启用/禁用用户", description = "需要 ADMIN 角色")
    @PatchMapping("/{id}/enabled")
    public ApiResponse<UserResponse> updateEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        return ApiResponse.success("状态更新成功", userService.updateEnabled(id, enabled));
    }
}
