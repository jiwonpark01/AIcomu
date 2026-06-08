# 프로젝트 규칙

## 주제
AI 지식 공유 + 최신 동향 커뮤니티 게시판

## 스택
- Java 17, Spring Boot 3.x, Gradle
- Spring Web, Spring Security, Spring JDBC, Thymeleaf, MariaDB, Lombok, Validation

## 패키지 구조 (기본 루트: aicommunity)
- 메인 클래스(@SpringBootApplication)는 aicommunity 바로 아래.
- config      : SecurityConfig 등 설정
- security    : UserDetailsService, 커스텀 UserDetails (인증 연결부)
- controller  : HTTP 요청/응답만
- service     : 비즈니스 로직, @Transactional, 권한 체크
- repository  : JdbcTemplate 기반 SQL 직접 작성
- domain      : 순수 Java 클래스(엔티티 역할). JPA 어노테이션 없음.
- dto/user    : 유저 관련 요청/응답 DTO
- dto/post    : 게시글 관련 요청/응답 DTO
- exception   : 커스텀 예외 + GlobalExceptionHandler

## 아키텍처 (반드시 지킬 것)
- Controller : HTTP 요청/응답, DTO 검증, Service 호출만. 비즈니스 로직 금지.
- Service    : 비즈니스 로직, @Transactional, 권한 체크. domain↔DTO 변환.
- Repository : JdbcTemplate만 사용. SQL 직접 작성. 비즈니스 로직 금지.
- domain 객체를 Controller/View에 직접 노출 금지. 요청/응답 DTO 따로 둘 것.
- JPA, JpaRepository, @Entity, @Table 사용 금지. 순수 JDBC만.

## DB
- MariaDB 로컬, DB명: aicomu
- 테이블은 schema.sql로 직접 생성 (JPA 자동생성 없음)
- src/main/resources/schema.sql 에 CREATE TABLE 문 작성

## 학습 모드 (중요)
- 나는 스프링을 배우는 중이다. 코드 작성 전에 "무슨 파일을 만들/고칠지"와
  "왜 그렇게 하는지"를 먼저 짧게 설명해라.
- Repository의 SQL은 각 쿼리가 무슨 역할인지 주석으로 달아줘라.
- 작성 후에는 핵심 부분이 무슨 역할인지 설명으로 알려줘라.
- 한 번에 한 기능만. 내가 다음 단계를 요청하기 전까지 범위를 넘지 마라.