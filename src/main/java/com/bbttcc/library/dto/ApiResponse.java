package com.bbttcc.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * 统一 API 响应包装。
 *
 * @param code    业务状态码（成功为 200，失败为对应 HTTP 状态码）
 * @param message 提示信息
 * @param data    业务数据
 * @param timestamp 服务器时间
 */
@Schema(description = "统一响应结构")
public record ApiResponse<T>(
        @Schema(description = "状态码", example = "200") int code,
        @Schema(description = "提示信息", example = "success") String message,
        @Schema(description = "业务数据") T data,
        @Schema(description = "时间戳") Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now());
    }
}
