package com.bbttcc.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "注册请求")
public record RegisterRequest(

        @Schema(description = "用户名", example = "alice")
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在 3-50 之间")
        String username,

        @Schema(description = "密码", example = "secret123")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度需在 6-100 之间")
        String password,

        @Schema(description = "真实姓名", example = "张三")
        @NotBlank(message = "姓名不能为空")
        @Size(max = 100, message = "姓名长度不能超过 100")
        String fullName,

        @Schema(description = "邮箱", example = "alice@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 120, message = "邮箱长度不能超过 120")
        String email
) {
}
