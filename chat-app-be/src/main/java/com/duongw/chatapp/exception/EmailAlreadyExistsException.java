package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_ALREADY_EXISTS", String.format("Email %s is already existed", email));
    }
}
