package com.bbttcc.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常基类。携带 HTTP 状态码，由 {@link GlobalExceptionHandler} 统一转换为响应。
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
