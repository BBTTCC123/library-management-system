package com.bbttcc.library.exception;

import org.springframework.http.HttpStatus;

/**
 * 资源冲突（如用户名/ISBN 已存在）-> HTTP 409。
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
