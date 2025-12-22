package com.wareland.common.exception;

/**
 * Exception untuk resource yang tidak ditemukan.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
