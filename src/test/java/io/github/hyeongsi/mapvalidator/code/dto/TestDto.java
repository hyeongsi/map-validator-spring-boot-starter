package io.github.hyeongsi.mapvalidator.code.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestDto {

    // 1. 문자열 검증
    @NotBlank(message = "이름은 필수이며 공백일 수 없습니다.")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Size(min = 2, max = 10, message = "별명은 2자 이상 10자 이하여야 합니다.")
    private String nickname;

    // 2. 숫자 검증
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    @Max(value = 100, message = "수량은 100 이하여야 합니다.")
    private Integer quantity;

    @Positive(message = "가격은 양수여야 합니다.")
    private Long price;

    // 3. 컬렉션 검증
    @NotEmpty(message = "태그는 최소 하나 이상 포함되어야 합니다.")
    @Size(max = 3, message = "태그는 최대 3개까지만 가능합니다.")
    private List<String> tags;

    // 4. 로직 검증
    @AssertTrue(message = "서비스 이용 약관에 동의해야 합니다.")
    private boolean termsAgreed;

    // 5. 어노테이션이 없는 필드 (검증 제외 대상)
    private String description;
    private String extraNote;
}
