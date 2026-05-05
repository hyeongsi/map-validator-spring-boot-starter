package io.github.hyeongsi.mapvalidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
    (
    classes = MapValidatorTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Slf4j
class ValidateMapAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Jakarta Validation의 모든 어노테이션 제약 조건 위반을 검증한다")
    void allJakartaAnnotations_Validation_Fail() throws Exception {
        // given: 모든 어노테이션 조건을 위반하는 데이터 구성
        Map<String, Object> request = new HashMap<>();
        request.put("name", "");                 // @NotBlank 위반
        request.put("email", "invalid-email");   // @Email 위반
        request.put("nickname", "a");            // @Size(min=2) 위반
        request.put("quantity", 0);              // @Min(1) 위반
        request.put("price", -100);              // @Positive 위반
        request.put("tags", List.of());          // @NotEmpty 위반
        request.put("termsAgreed", false);       // @AssertTrue 위반

        // when: 요청 실행
        Throwable ex = catchThrowable(() ->
            mockMvc.perform(post("/api/v1/validation/auto-exception")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        );

        // then: 결과 검증
        assertThat(ex).isInstanceOf(ServletException.class);
        Throwable cause = ex.getCause();
        String message = cause.getMessage();

        assertThat(message)
            .contains("이름은 필수이며 공백일 수 없습니다.")
            .contains("이메일 형식이 올바르지 않습니다.")
            .contains("별명은 2자 이상 10자 이하여야 합니다.")
            .contains("수량은 1 이상이어야 합니다.")
            .contains("가격은 양수여야 합니다.")
            .contains("태그는 최소 하나 이상 포함되어야 합니다.")
            .contains("서비스 이용 약관에 동의해야 합니다.");

        log.info("cause: ", cause);
    }

    @Test
    @DisplayName("어노테이션이 없는 필드는 어떤 값이 들어와도 검증을 통과한다")
    void noAnnotationFields_Always_Pass() throws Exception {
        // given: 필수 값은 채우고, 어노테이션 없는 필드에 특이값 주입
        Map<String, Object> request = new HashMap<>();
        request.put("name", "정상사용자");
        request.put("quantity", 10);
        request.put("tags", List.of("user"));          // @NotEmpty 라서 무조건 하나 이상 있어야함
        request.put("termsAgreed", true);

        // 어노테이션이 없는 필드들
        request.put("description", ""); // 빈 값이어도 통과
        request.put("extraNote", "A".repeat(1000)); // 매우 길어도 통과

        // when & then: 성공 응답 확인
        mockMvc.perform(post("/api/v1/validation/auto-exception")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("success"));
    }

    @Test
    @DisplayName("MapValidationResult를 사용할 때 모든 에러 메시지가 리스트에 담기는지 확인한다")
    void manualResult_CaptureAllErrors() throws Exception {
        // given: 위반 데이터
        Map<String, Object> request = Map.of(
            "name", "",
            "quantity", -5
        );

        // when & then: 200 응답과 함께 에러 메시지 포함 여부 확인
        mockMvc.perform(post("/api/v1/validation/manual-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(result -> {
                String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                log.info(">>>> [MockMvc Response Body]: {}", content);
                log.info(">>>> [Status]: {}", result.getResponse().getStatus());
            })
            .andExpect(status().isOk())
            .andExpectAll(
                content().string(Matchers.containsString("이름은 필수이며 공백일 수 없습니다.")),
                content().string(Matchers.containsString("서비스 이용 약관에 동의해야 합니다.")),
                content().string(Matchers.containsString("태그는 최소 하나 이상 포함되어야 합니다.")),
                content().string(Matchers.containsString("수량은 1 이상이어야 합니다.")));
    }
}