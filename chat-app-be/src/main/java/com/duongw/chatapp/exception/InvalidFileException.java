package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class InvalidFileException extends BaseException {

    public InvalidFileException(String message) {
        super("INVALID_FILE", message);
    }
}
