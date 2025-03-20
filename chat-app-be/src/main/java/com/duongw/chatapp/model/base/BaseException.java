package com.duongw.chatapp.model.base;


import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final String errorCode;
    private final String message;

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }
}
