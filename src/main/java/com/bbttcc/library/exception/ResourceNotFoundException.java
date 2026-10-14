package com.bbttcc.library.exception;

import org.springframework.http.HttpStatus;

/**
 * 资源不存在 -> HTTP 404。
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(resource + " 不存在: " + id);
    }
}
