package io.github.hyeongsi.mapvalidator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hyeongsi.mapvalidator.aspect.ValidateMapAspect;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
    prefix = "map-validator",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true   // 설정이 아예 없으면 true로 간주
)
@ConditionalOnClass(name = {
    "com.fasterxml.jackson.databind.ObjectMapper",
    "jakarta.validation.Validator"
})
public class MapValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean   // 사용자가 직접 Bean 만들면 override 안 함
    public ValidateMapAspect validateMapAspect(
        ObjectMapper objectMapper,
        Validator validator
    ) {
        // [INFO] 라이브러리 활성화 로깅
        log.info("[MapValidator] Initializing MapValidationAspect. Library is now active.");
        return new ValidateMapAspect(objectMapper, validator);
    }
}
