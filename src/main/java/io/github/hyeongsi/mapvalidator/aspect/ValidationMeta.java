package io.github.hyeongsi.mapvalidator.aspect;

public class ValidationMeta {
    final int index;
    final Class<?> dtoClass;
    final Class<?>[] groups;

    public ValidationMeta(int index, Class<?> dtoClass, Class<?>[] groups) {
        this.index = index;
        this.dtoClass = dtoClass;
        this.groups = groups;
    }
}
