package io.github.hyeongsi.mapvalidator.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hyeongsi.mapvalidator.annotation.ValidateMap;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class ValidateMapAspect {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    private final Map<Method, List<ValidationMeta>> cache = new ConcurrentHashMap<>();

    @Around("within(@org.springframework.web.bind.annotation.RestController *) ||" +
        "within(@org.springframework.stereotype.Controller *)")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), joinPoint.getTarget().getClass());

        List<ValidationMeta> metas = cache.computeIfAbsent(method, this::extractMeta);

        if (metas.isEmpty()) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();

        for (ValidationMeta meta : metas) {

            Object arg = args[meta.index];

            if (!(arg instanceof Map<?, ?> param)) {
                throw new IllegalArgumentException("@ValidateMap must be used on Map parameter");
            }

            Object dto;
            try {
                dto = objectMapper.convertValue(param, meta.dtoClass);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid request format", e);
            }

            Set<ConstraintViolation<Object>> validate = validator.validate(dto);

            if (!validate.isEmpty()) {
                throw new ConstraintViolationException(validate);
            }
        }

        return joinPoint.proceed();
    }

    private List<ValidationMeta> extractMeta(Method method) {

        List<ValidationMeta> result = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {

            ValidateMap annotation = parameters[i].getAnnotation(ValidateMap.class);
            if (annotation == null) continue;

            ValidationMeta meta = new ValidationMeta(i, annotation.value());
            result.add(meta);
        }

        return result;
    }

    static class ValidationMeta {
        final int index;
        final Class<?> dtoClass;

        public ValidationMeta(int index, Class<?> dtoClass) {
            this.index = index;
            this.dtoClass = dtoClass;
        }
    }
}
