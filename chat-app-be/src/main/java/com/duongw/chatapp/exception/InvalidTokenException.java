package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
}
