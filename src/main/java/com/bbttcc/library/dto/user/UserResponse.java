package com.bbttcc.library.dto.user;

import com.bbttcc.library.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "用户信息")
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt
) {
}
