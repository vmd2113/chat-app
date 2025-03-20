package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("NOT_FOUND", String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
