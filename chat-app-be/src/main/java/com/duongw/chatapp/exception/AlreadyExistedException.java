package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class AlreadyExistedException extends BaseException {

    public AlreadyExistedException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }

}
