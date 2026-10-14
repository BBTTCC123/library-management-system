package com.bbttcc.library.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务规则不满足 -> HTTP 400。
 */
public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
