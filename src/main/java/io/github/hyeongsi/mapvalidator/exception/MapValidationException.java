package io.github.hyeongsi.mapvalidator.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class MapValidationException extends RuntimeException{

    private final List<String> errorMessages;

    public MapValidationException(List<String> errorMessages) {
        super("Map 데이터 유효성 검사 실패: " + errorMessages.toString());
        this.errorMessages = errorMessages;
    }
}
