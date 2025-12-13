package com.wareland.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that a password contains at least:
 * - one uppercase letter,
 * - one digit,
 * - one special character (e.g., @, !, #, etc.).
 *
 * Length requirements should be enforced separately (e.g., with {@code @Size})
 * to keep this validator focused and reusable.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface StrongPassword {

    String message() default "Password must contain at least one uppercase letter, one digit, and one special character (e.g., @).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
