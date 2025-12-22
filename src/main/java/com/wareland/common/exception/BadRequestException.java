package com.wareland.common.exception;

/**
 * Exception untuk request tidak valid dari client.
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(message);
    }
}
