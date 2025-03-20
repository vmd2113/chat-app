package com.duongw.chatapp.exception;

import com.duongw.chatapp.model.base.BaseException;

public class CalendarException  extends BaseException {

    public CalendarException(String message) {
        super("CALENDAR_ERROR", message);
    }

    public CalendarException(String message, Throwable cause) {
        super("CALENDAR_ERROR", message, cause);
    }
}
