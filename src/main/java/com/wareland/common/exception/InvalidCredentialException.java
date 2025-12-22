package com.wareland.common.exception;

/**
 * Exception untuk kesalahan autentikasi akibat kredensial tidak valid.
 */
public class InvalidCredentialException extends BusinessException {

    public InvalidCredentialException(String message) {
        super(message);
    }
}
