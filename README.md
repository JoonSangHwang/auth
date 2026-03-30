# Auth Server

가벼운 OAuth 기반 인증 서버입니다.

---

## 기술 스택

| 항목 | 버전 |
|---|---|
| JDK | 1.8 |
| Spring Boot | 2.7.18 |
| Kotlin | 1.8.22 |
| Gradle | 7.6.4 |
| Spring Security | 5.7.11 |
| io.spring.dependency-management | 1.1.4 |
| jjwt | 0.11.5 |
| springdoc-openapi | 1.7.0 |
| H2 (local) | Spring Boot 관리 |

---

## 프로젝트 구조

```
src/main/kotlin/com/hmdf/auth/
├── config/
│   ├── DataInitializer.kt        # local 환경 테스트 데이터 삽입
│   ├── SecurityConfig.kt         # Spring Security 설정
│   ├── SwaggerConfig.kt          # Swagger 설정
│   └── WebConfig.kt              # 인터셉터 등록
├── controller/
│   ├── AuthController.kt         # 토큰 발급 / 폐기
│   └── MemberController.kt       # 사용자 정보 조회
├── domain/
│   ├── Member.kt                 # 회원 엔티티
│   ├── OAuthClient.kt            # 클라이언트 enum
│   └── Profile.kt                # 프로파일 상수
├── dto/
│   ├── ApiResponse.kt            # 공통 응답 래퍼
│   ├── LoginRequest.kt
│   ├── MemberResponse.kt
│   └── TokenResponse.kt
├── exception/
│   ├── AuthException.kt          # 커스텀 예외 / 에러코드
│   └── GlobalExceptionHandler.kt
├── interceptor/
│   ├── RequireAuth.kt            # 인증 필요 어노테이션
│   └── TokenValidationInterceptor.kt
├── repository/
│   └── MemberRepository.kt
├── service/
│   ├── AuthService.kt
│   └── MemberService.kt
└── util/
    └── JwtUtil.kt

src/main/resources/
├── application.yaml              # 공통 설정
├── application-local.yaml
├── application-dev.yaml
├── application-stage.yaml
├── application-prod.yaml
└── templates/
    └── login.html                # Thymeleaf 로그인 화면

src/test/http/
├── auth.http                     # 인증 API 테스트
├── member.http                   # 회원 API 테스트
└── http-client.env.json          # 환경별 변수
```

---

## 환경 설정

| 환경 | 프로파일 | DB | 프로파일 미지정 시 |
|---|---|---|---|
| 로컬 | `local` | H2 인메모리 | 자동 활성화 |
| 개발 | `dev` | 환경변수 | - |
| 스테이지 | `stage` | 환경변수 | - |
| 운영 | `prod` | 환경변수 | - |

### 실행 방법

```bash
# 프로파일 미지정 시 local 자동 활성화
./gradlew bootRun

# 프로파일 지정
./gradlew bootRun --args='--spring.profiles.active=dev'

# JAR 실행
java -jar auth.jar --spring.profiles.active=prod
```

### dev / stage / prod 환경변수

```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<32자 이상>
DB_URL=jdbc:mysql://...
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_USERNAME=...
DB_PASSWORD=...
```

---

## API 명세

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 인증

| Method | URL | 설명 | 인증 필요 |
|---|---|---|---|
| GET | `/login` | 로그인 페이지 | X |
| POST | `/oauth/token` | 토큰 발급 (로그인) | X |
| POST | `/oauth/revoke` | 토큰 폐기 (로그아웃) | X |

### 회원

| Method | URL | 설명 | 인증 필요 |
|---|---|---|---|
| GET | `/oauth/userinfo` | 사용자 정보 조회 | O |

### 공통 응답 형식

```json
// 성공
{
  "success": true,
  "data": { },
  "message": "성공"
}

// 실패
{
  "success": false,
  "code": "AUTH-001",
  "message": "아이디 또는 비밀번호가 올바르지 않습니다."
}
```

### 에러 코드

| 코드 | 설명 |
|---|---|
| `AUTH-001` | 아이디 또는 비밀번호 불일치 |
| `AUTH-002` | 토큰 없음 |
| `AUTH-003` | 유효하지 않은 토큰 |
| `AUTH-004` | 만료된 토큰 |
| `AUTH-005` | 등록되지 않은 클라이언트 |
| `SERVER-001` | 서버 내부 오류 |

---

## OAuth 클라이언트

| 클라이언트 | clientId |
|---|---|
| WEB_QUERY | `550e8400-e29b-41d4-a716-446655440000` |
| MASTER_MODEL | `550e8400-e29b-41d4-a716-446655440001` |
| LOCAL | `550e8400-e29b-41d4-a716-446655440002` |

- 각 클라이언트는 환경별 `redirectUrl` 보유 (`OAuthClient.kt` 참고)
- 로그인 성공 시 해당 환경의 `redirectUrl`로 리다이렉트

---

## 토큰

| 항목 | 내용 |
|---|---|
| 알고리즘 | HS256 |
| Access Token 만료 | 발급 당일 23:59:59 |
| Refresh Token 만료 | 발급 후 7일 |
| 전달 방식 | HttpOnly 쿠키 (`access_token`, `refresh_token`) |
| 검증 방식 | `@RequireAuth` 어노테이션 → `TokenValidationInterceptor` |

---

## 로컬 개발

### H2 콘솔
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:authdb
Username: sa
Password: (없음)
```

### 테스트 계정
```
아이디: admin
비밀번호: 1
사번: EMP0001
```
