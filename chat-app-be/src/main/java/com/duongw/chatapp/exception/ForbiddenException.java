package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}
