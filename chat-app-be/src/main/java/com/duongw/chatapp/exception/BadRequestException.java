package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }

}
