package io.github.hyeongsi.mapvalidator.code.controller;

import io.github.hyeongsi.mapvalidator.annotation.ValidateMap;
import io.github.hyeongsi.mapvalidator.code.dto.TestDto;
import io.github.hyeongsi.mapvalidator.result.MapValidationResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/validation")
public class TestController {

    /**
     * [기능 1] 자동 예외 처리 테스트
     * MapValidationResult가 파라미터에 없으므로, 검증 실패 시 ConstraintViolationException이 즉시 발생합니다.
     */
    @PostMapping("/auto-exception")
    public String validateWithException(
        @RequestBody @ValidateMap(TestDto.class) Map<String, Object> param
    ) {
        return "success";
    }

    /**
     * [기능 2] 수동 결과 핸들링 테스트
     * MapValidationResult가 파라미터에 존재하므로, 예외를 던지지 않고 에러 정보를 result 객체에 담습니다.
     */
    @PostMapping("/manual-result")
    public String validateWithResult(
        @RequestBody @ValidateMap(TestDto.class) Map<String, Object> param,
        MapValidationResult result
    ) {

        if (result.hasErros()) {
            return "error: " + result.getErrorMessages();
        }
        return "success";
    }
}
