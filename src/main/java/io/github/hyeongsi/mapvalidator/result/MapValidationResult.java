package io.github.hyeongsi.mapvalidator.result;

import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapValidationResult {

    private final List<ConstraintViolation<Object>> errors = new ArrayList<>();

    public void addErrors(Set<ConstraintViolation<Object>> violations) {
        this.errors.addAll(violations);
    }

    public boolean hasErros() {
        return !errors.isEmpty();
    }

    public List<ConstraintViolation<Object>> getAllErrors() {
        return errors;
    }

    public List<String> getErrorMessages() {
        return errors.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
    }
}
