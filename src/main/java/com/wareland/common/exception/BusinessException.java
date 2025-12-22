package com.wareland.common.exception;

/**
 * Base exception untuk seluruh error logika bisnis aplikasi.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
