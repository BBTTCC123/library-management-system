package com.bbttcc.library.mapper;

import com.bbttcc.library.dto.user.UserResponse;
import com.bbttcc.library.entity.User;

/**
 * 用户实体与 DTO 之间的转换。
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
