package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class UsernameAlreadyExistsException extends BaseException {
    public UsernameAlreadyExistsException(String username) {
        super("USERNAME_ALREADY_EXISTS", String.format("Username %s is already taken", username));
    }
}
