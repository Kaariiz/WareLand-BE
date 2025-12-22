package com.wareland.common.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotasi validasi untuk memastikan password memenuhi kriteria keamanan.
 * Validasi panjang password dilakukan terpisah (misalnya dengan @Size).
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface StrongPassword {

    /**
     * Pesan error default jika validasi gagal.
     */
    String message() default
            "Password must contain at least one uppercase letter, one digit, and one special character (e.g., @).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
