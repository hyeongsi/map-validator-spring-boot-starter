package io.github.hyeongsi.mapvalidator.result;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class MapValidationResult {

    private final List<ConstraintViolation<Object>> errors = new ArrayList<>();

    public void addErrors(Set<ConstraintViolation<Object>> violations) {
        this.errors.addAll(violations);
        // 에러가 추가될 대 요약 정보 기록
        if (log.isDebugEnabled()) {
            log.debug("[MapValidator] Added {} validation errors. Total errors: {}", violations.size(), this.errors.size());
        }
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
