package com.bbttcc.library.security;

import com.bbttcc.library.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户的工具类。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return securityUser;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static boolean isCurrentUserAdmin() {
        return currentUser().getRole() == com.bbttcc.library.entity.Role.ADMIN;
    }

    public static boolean canManageLibrary() {
        var role = currentUser().getRole();
        return role == com.bbttcc.library.entity.Role.ADMIN || role == com.bbttcc.library.entity.Role.LIBRARIAN;
    }
}
