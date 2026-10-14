package com.bbttcc.library.dto.user;

import com.bbttcc.library.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理员修改用户角色请求")
public record UpdateRoleRequest(

        @Schema(description = "新角色", example = "LIBRARIAN")
        @NotNull(message = "角色不能为空")
        Role role
) {
}
