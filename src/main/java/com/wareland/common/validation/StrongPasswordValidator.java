package com.wareland.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null/blank handled by @NotBlank when needed. For optional fields, allow null/blank.
        if (value == null || value.isBlank()) {
            return true;
        }
        return UPPERCASE.matcher(value).matches()
                && DIGIT.matcher(value).matches()
                && SPECIAL.matcher(value).matches();
    }
}
