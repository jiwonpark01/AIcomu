# AI Community — 개발 보고서

url: https://race-boston-coverage-nail.trycloudflare.com

---

## 1. 프로젝트 개요

**수행 주제:** Thymeleaf 기반 AI 지식 공유 및 최신 동향 커뮤니티 게시판

**서비스 설명:**  
AI 관련 지식·트렌드·질문을 카테고리별로 작성하고 공유하는 커뮤니티 게시판.  
비로그인 사용자도 게시글 조회가 가능하며, 글 작성·수정·삭제는 로그인한 본인만 가능.

**사용 기술:**

| 분류 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 4.x, Spring Security 7.x, Spring JDBC (JdbcTemplate) |
| Frontend | Thymeleaf, Thymeleaf Security Extras, HTML/CSS |
| Database | MariaDB |
| 인프라 | GCP VM (e2-micro), Cloudflare Tunnel |
| 빌드 | Gradle |

---

## 2. 스프링 아키텍처 및 서비스 구조

### 계층 구조 설계 개요

```
[Browser]
    ↓ HTTP 요청
[Controller]   — 요청/응답 처리, DTO 검증, Service 호출만 담당
    ↓
[Service]      — 비즈니스 로직, 권한 체크, @Transactional, domain ↔ DTO 변환
    ↓
[Repository]   — JdbcTemplate으로 SQL 직접 작성 및 실행
    ↓
[MariaDB]
```

- **domain 객체**는 DB 테이블과 1:1 대응하는 순수 Java 클래스 (JPA 어노테이션 없음)
- **Controller**는 domain 객체를 View에 직접 노출하지 않고, 반드시 DTO(PostResponse 등)를 사용
- **Service**에서 소유권 체크(본인 글인지 확인) 후 수정·삭제 허용

### 패키지 구조

```
aicommunity/
├── config/          SecurityConfig — Spring Security 설정
├── controller/      UserController, PostController — HTTP 요청 처리
├── service/         UserService, PostService — 비즈니스 로직
├── repository/      UserRepository, PostRepository — JdbcTemplate SQL
├── domain/          User, Post, Category, Role — 순수 엔티티 클래스
├── dto/
│   ├── user/        SignupRequest
│   └── post/        PostCreateRequest, PostUpdateRequest, PostResponse
├── security/        CustomUserDetails, CustomUserDetailsService
└── exception/       UnauthorizedException, GlobalExceptionHandler
```

### 주요 URL 및 권한 라우팅

| URL | 메서드 | 접근 권한 | 설명 |
|-----|--------|-----------|------|
| `/` | GET | 누구나 | `/posts`로 리다이렉트 |
| `/signup` | GET/POST | 누구나 | 회원가입 |
| `/login` | GET/POST | 누구나 | 로그인 |
| `/posts` | GET | 누구나 | 게시글 목록 |
| `/posts/{id}` | GET | 누구나 | 게시글 상세 |
| `/posts/new` | GET/POST | 로그인 필요 | 게시글 작성 |
| `/posts/{id}/edit` | GET/POST | 로그인 필요 (본인만) | 게시글 수정 |
| `/posts/{id}/delete` | POST | 로그인 필요 (본인만) | 게시글 삭제 |
| `/logout` | POST | 로그인 필요 | 로그아웃 |

---

## 3. Spring Security 인증/인가 설정

### 폼 로그인 설정

```java
http
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
        .requestMatchers("/signup", "/login", "/error").permitAll()
        .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
        .anyRequest().authenticated()
    )
    .formLogin(form -> form
        .loginPage("/login")
        .defaultSuccessUrl("/posts")
        .permitAll()
    )
    .logout(logout -> logout
        .logoutSuccessUrl("/login")
        .permitAll()
    );
```

### 인증 처리 흐름

1. 사용자가 `/login`에 username/password POST
2. Spring Security가 `CustomUserDetailsService.loadUserByUsername()` 호출
3. DB에서 username으로 유저 조회 → `CustomUserDetails` 반환
4. BCryptPasswordEncoder로 비밀번호 검증
5. 인증 성공 시 세션 생성 후 `/posts`로 리다이렉트

### 세션 관리

- Spring Security 기본 세션 방식 사용 (서버 측 세션)
- 로그인 성공 시 세션에 인증 정보 저장
- 로그아웃 시 세션 무효화

### 비밀번호 암호화

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

회원가입 시 BCrypt로 해싱하여 저장, 로그인 시 해시값 비교.

### 권한 체계

| 권한 | 설명 |
|------|------|
| `USER` | 일반 회원 (기본값). 게시글 CRUD 가능 (본인 글만 수정·삭제) |

---

## 4. 데이터베이스 및 SQL 활용

### 사용 테이블

**users** — 회원 정보

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 회원 고유 식별자 |
| username | VARCHAR(50) UNIQUE | 로그인 ID |
| password | VARCHAR(255) | BCrypt 암호화 비밀번호 |
| nickname | VARCHAR(50) | 화면 표시 이름 |
| role | VARCHAR(20) DEFAULT 'USER' | 권한 |

**posts** — 게시글

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 게시글 고유 식별자 |
| title | VARCHAR(200) | 제목 |
| content | TEXT | 본문 |
| category | VARCHAR(20) | 카테고리 (KNOWLEDGE/TREND/QNA) |
| author_id | BIGINT (FK → users.id) | 작성자 |
| created_at | DATETIME DEFAULT NOW() | 작성일시 |
| updated_at | DATETIME DEFAULT NOW() | 수정일시 |

### 주요 SQL

```sql
-- 회원 등록
INSERT INTO users (username, password, nickname, role)
VALUES (?, ?, ?, 'USER')

-- 로그인용 유저 조회 (Spring Security 인증)
SELECT id, username, password, nickname, role
FROM users WHERE username = ?

-- 게시글 전체 조회 (최신순)
SELECT id, title, content, category, author_id, created_at, updated_at
FROM posts ORDER BY created_at DESC

-- 게시글 단건 조회
SELECT id, title, content, category, author_id, created_at, updated_at
FROM posts WHERE id = ?

-- 게시글 등록
INSERT INTO posts (title, content, category, author_id, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?)

-- 게시글 수정
UPDATE posts SET title = ?, content = ?, category = ?, updated_at = ?
WHERE id = ?

-- 게시글 삭제
DELETE FROM posts WHERE id = ?
```

---

## 5. 트러블슈팅 (문제 해결 기록)

### 🔴 사례 1 — Thymeleaf `_csrf` NullPointerException으로 로그인 페이지 500 오류

**문제:**  
로그인 버튼을 누르면 Whitelabel Error Page (status=500) 발생.  
로그에 `EL1007E: Property or field 'parameterName' cannot be found on null` 출력.

**원인:**  
`login.html`에 CSRF 토큰 hidden input이 있었는데, `SecurityConfig`에서 `csrf.disable()`을 설정하면 Thymeleaf의 `${_csrf}` 객체가 null이 되어 NullPointerException 발생.

```html
<!-- 문제가 된 코드 -->
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

**해결:**  
`login.html`, `list.html`, `detail.html`, `edit.html`, `form.html` 전체에서 `_csrf` hidden input 제거.

---

### 🔴 사례 2 — Cloudflare 터널 환경에서 로그인 후 status=999 오류

**문제:**  
로그인 인증은 성공(DB 쿼리 정상)하지만, 이후 `/posts` 리다이렉트 시 Cloudflare가 status=999 반환.

**원인:**  
Cloudflare 터널은 클라이언트와 HTTPS로 통신하지만, Spring Boot는 이를 모르고 `http://localhost:3000/posts`로 리다이렉트 URL을 생성. Cloudflare가 잘못된 URL을 받아 연결을 끊음.

**해결:**  
`application.yml`에 forwarded header 신뢰 설정 추가.

```yaml
server:
  port: 3000
  forward-headers-strategy: native
```

`X-Forwarded-Proto`, `X-Forwarded-Host` 헤더를 신뢰하도록 설정하여 Spring이 터널 도메인 기준으로 올바른 리다이렉트 URL을 생성.

---

### 🟡 사례 3 — GCP VM 메모리 부족(OOM)으로 서버 강제 종료

**문제:**  
`./gradlew bootRun` 실행 중 프로세스가 `exit 137`(OOM Killer)로 종료됨.

**원인:**  
GCP e2-micro VM은 메모리가 1GB로 제한되어 있는데, Gradle 데몬 + Spring Boot JVM이 동시에 실행되며 메모리 초과 발생.

**해결:**  
`bootRun` 대신 JAR 파일을 빌드 후 메모리 제한을 명시하여 실행.

```bash
./gradlew bootJar
nohup java -Xmx256m -jar build/libs/aicommunity-*.jar > app.log 2>&1 &
disown -a
```

---

### 🟡 사례 4 — Spring Boot 4.x에서 ErrorController 인터페이스 제거

**문제:**  
커스텀 에러 컨트롤러 작성 시 `package org.springframework.boot.web.servlet.error does not exist` 컴파일 에러.

**원인:**  
Spring Boot 4.x에서 `ErrorController` 인터페이스가 제거됨.

**해결:**  
커스텀 에러 컨트롤러 파일을 삭제하고, `templates/error/404.html`, `templates/error/403.html`을 직접 생성. Spring Boot가 `templates/error/{상태코드}.html` 파일을 자동으로 찾아서 렌더링하는 기본 동작을 활용.
