package io.github.hyeongsi.mapvalidator.aspect;

public class ValidationMeta {
    final int index;
    final Class<?> dtoClass;

    public ValidationMeta(int index, Class<?> dtoClass) {
        this.index = index;
        this.dtoClass = dtoClass;
    }
}
