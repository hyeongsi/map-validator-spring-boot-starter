package io.github.hyeongsi.mapvalidator.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hyeongsi.mapvalidator.annotation.ValidateMap;
import io.github.hyeongsi.mapvalidator.result.MapValidationResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class ValidateMapAspect {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    private final Map<Method, List<ValidationMeta>> cache = new ConcurrentHashMap<>();

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) ||" +
        "within(@org.springframework.stereotype.Controller *)")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), joinPoint.getTarget().getClass());

        // 캐시 확인 로깅
        if (cache.containsKey(method)) {
            log.trace("[MapValidator] Cache HIT for method: {}", method.getName());
        } else {
            log.trace("[MapValidator] Cache MISS for method: {}", method.getName());
        }

        List<ValidationMeta> metas = cache.computeIfAbsent(method, m -> {
            // [DEBUG] 캐시 확인 및 메타데이터 추출 로깅
            log.debug("[MapValidator] Extracting metadata for method: {}", m.getName());
            return this.extractMeta(m);
        });

        if (metas.isEmpty()) {
            return joinPoint.proceed();
        }

        log.trace("[MapValidator] Starting validation for method: {}", method.getName());

        Object[] args = joinPoint.getArgs();

        MapValidationResult validationResult = null;
        for (Object arg : args) {

            if (arg instanceof MapValidationResult) {
                validationResult = (MapValidationResult) arg;
                break;
            }
        }

        for (ValidationMeta meta : metas) {

            Object arg = args[meta.index];

            if (!(arg instanceof Map<?, ?> param)) {
                // [ERROR] 잘못된 사용 사례 로깅
                log.error("[MapValidator] Critical Error: @ValidateMap assigned to non-Map parameter at index {}", meta.index);
                throw new IllegalArgumentException("@ValidateMap must be used on Map parameter");
            }

            // [DEBUG] 변환 과정 로깅
            log.debug("[MapValidator] Converting Map to DTO: {} using groups: {}", meta.dtoClass.getSimpleName(), Arrays.toString(meta.groups));

            Object dto = objectMapper.convertValue(param, meta.dtoClass);
            Set<ConstraintViolation<Object>> validate = validator.validate(dto, meta.groups);

            if (!validate.isEmpty()) {
                // [WARN] 검증 실패 로깅
                log.warn("[MapValidator] Validation failed for method[{}]. Violations count: {}", method.getName(), validate.size());

                // 상세 오류 trace 로깅 (개발 시 확인 용도)
                if (log.isTraceEnabled()) {
                    validate.forEach(v -> log.trace("[MapValidator] Detail: field={}, message={}", v.getPropertyPath(), v.getMessage()));
                }

                if (validationResult != null) {

                    validationResult.addErrors(validate);
                } else {
                    throw new ConstraintViolationException(validate);
                }
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

            ValidationMeta meta = new ValidationMeta(i, annotation.value(), annotation.groups());
            result.add(meta);
        }

        return result;
    }
}
