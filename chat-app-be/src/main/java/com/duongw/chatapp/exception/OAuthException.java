package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class OAuthException extends BaseException {
    public OAuthException(String message) {
        super("OAUTH_ERROR", message);
    }

    public OAuthException(String message, Throwable cause) {
        super("OAUTH_ERROR", message, cause);
    }
}
