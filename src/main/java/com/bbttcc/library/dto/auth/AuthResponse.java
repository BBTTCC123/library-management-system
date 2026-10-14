package com.bbttcc.library.dto.auth;

import com.bbttcc.library.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录/注册成功后的令牌响应")
public record AuthResponse(
        @Schema(description = "JWT 访问令牌") String accessToken,
        @Schema(description = "令牌类型", example = "Bearer") String tokenType,
        @Schema(description = "过期时间（毫秒）", example = "86400000") long expiresIn,
        @Schema(description = "当前用户信息") UserResponse user
) {
    public static AuthResponse of(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
