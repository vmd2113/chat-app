package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
