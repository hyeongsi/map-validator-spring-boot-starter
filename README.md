# 🗺️ Map Validator Spring Boot Starter

Spring Boot 환경에서 `Map<String, Object>` 형태의 데이터를 DTO 클래스 기반으로 간편하게 검증하기 위한 커스텀 스타터 라이브러리입니다. JSON 데이터가 동적이거나 가변적인 파라미터를 다룰 때, 비즈니스 로직과 검증 로직을 깔끔하게 분리해줍니다.

## ✨ 주요 기능
*   **Map to DTO 검증**: `Map` 데이터를 원하는 DTO 클래스로 자동 변환 및 검증 수행.
*   **Validation Groups 지원**: 상황(생성, 수정 등)에 따라 동일한 DTO 내에서 검증 규칙을 다르게 적용 가능.
*   **유연한 에러 처리**: 검증 실패 시 예외를 직접 던지거나, `MapValidationResult`를 통해 컨트롤러 내에서 수동 제어 가능.
*   **AOP 기반 설계**: `@RestController` 및 `@Controller` 내의 메서드를 가로채어 자동으로 검증 로직 수행.

## 📦 의존성 추가 (Maven)

보안 취약점이 패치된 Spring Boot 3.4.3 이상 사용을 권장합니다.
```xml
<dependency>
    <groupId>io.github.hyeongsi</groupId>
    <artifactId>map-validator-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
## 🚀 사용 방법 (Usage)
### 1. DTO 및 Validation Group 정의
상황별 검증을 위해 마커 인터페이스(Groups)와 제약 조건이 선언된 DTO를 작성합니다.

```java
// 1. 검증 그룹용 인터페이스 정의
public interface OnCreate {}
public interface OnUpdate {}

// 2. DTO 정의
public class UserRequest {
    @NotBlank(groups = OnCreate.class) // 생성 시에만 필수
    private String id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class}) // 공통 필수
    @Size(min = 2, max = 10)
    private String name;

    @Email
    private String email;

    // Getter, Setter 필수 (Jackson 변환용)
}
```

### 2. 컨트롤러 적용 예시
**방법 A: 자동 예외 처리 (Default)**

검증 실패 시 `ConstraintViolationException`이 즉시 발생합니다.

```java
@PostMapping("/register")
public String register(
    @RequestBody @ValidateMap(value = UserRequest.class, groups = OnCreate.class) Map<String, Object> params
) {
    return "success";
}
```

**방법 B: 수동 에러 처리 (`MapValidationResult`)**

예외를 던지지 않고, 컨트롤러 내부에서 직접 에러 메시지를 가공할 수 있습니다.
```java
@PostMapping("/update")
public ResponseEntity<?> update(
    @RequestBody @ValidateMap(UserRequest.class) Map<String, Object> params,
    MapValidationResult result // 결과를 담을 객체 주입
) {
    if (result.hasErrors()) {
        // 원하는 형태의 에러 응답 구성 가능
        return ResponseEntity.badRequest().body(result.getErrorMessages());
    }
    return ResponseEntity.ok("update success");
}
```

---

## 🚨 문제 해결 및 FAQ

### 1. `ConstraintViolationException`이 발생합니다.
* **원인**: `@ValidateMap`을 통한 검증 과정에서 제약 조건 위반이 발견되었으나, `MapValidationResult` 파라미터가 선언되지 않았을 때 발생합니다.
* **해결**: 전역 예외 처리기(`@RestControllerAdvice`)에서 해당 예외를 캐치하여 응답하거나, 컨트롤러 메서드 인자에 `MapValidationResult`를 추가하여 수동으로 처리하세요.

### 2. `IllegalArgumentException` 발생
* **메시지**: `@ValidateMap must be used on Map parameter`
* **원인**: 해당 어노테이션을 `Map`이 아닌 타입(String, List 등)에 붙였을 경우 발생합니다.
* **해결**: 반드시 `Map<String, ?>` 타입의 파라미터에만 어노테이션을 사용하세요.

### 3. 검증이 전혀 동작하지 않습니다.
* **DTO 필드 확인**: DTO 클래스에 **Getter/Setter**가 누락되면 Jackson이 데이터를 채울 수 없어 검증이 정상적으로 이루어지지 않습니다.
* **애노테이션 위치**: `@ValidateMap`이 컨트롤러(`@Controller`, `@RestController`) 내의 메서드 파라미터에 정확히 위치했는지 확인하세요.

### 4. 보안 취약점 경고가 뜹니다.
* **해결**: 본 라이브러리는 최신 보안 패치를 위해 **Spring Boot 3.4.3** 이상을 기반으로 설정되어 있습니다. 프로젝트의 `pom.xml` 부모(parent) 버전을 최신으로 유지하시기 바랍니다.
