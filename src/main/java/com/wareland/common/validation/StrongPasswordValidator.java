package com.wareland.common.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator untuk anotasi @StrongPassword.
 */
public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern UPPERCASE =
            Pattern.compile(".*[A-Z].*");

    private static final Pattern DIGIT =
            Pattern.compile(".*\\d.*");

    private static final Pattern SPECIAL =
            Pattern.compile(".*[^A-Za-z0-9].*");

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        // Null atau blank diizinkan untuk field opsional
        if (value == null || value.isBlank()) {
            return true;
        }

        return UPPERCASE.matcher(value).matches()
                && DIGIT.matcher(value).matches()
                && SPECIAL.matcher(value).matches();
    }
}
