CREATE TABLE IF NOT EXISTS users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,  -- PK, 자동 증가
    username VARCHAR(50)  NOT NULL UNIQUE,           -- 로그인 ID, 중복 불가
    password VARCHAR(255) NOT NULL,                  -- BCrypt 암호화된 비밀번호 (길이 60~72자)
    nickname VARCHAR(50)  NOT NULL,                  -- 화면에 표시할 이름
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER',   -- 권한 (USER / ADMIN)
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS posts (
    id         BIGINT       NOT NULL AUTO_INCREMENT,   -- PK, 자동 증가
    title      VARCHAR(200) NOT NULL,                  -- 게시글 제목
    content    TEXT         NOT NULL,                  -- 게시글 본문 (긴 텍스트)
    category   VARCHAR(20)  NOT NULL,                  -- 카테고리 (KNOWLEDGE/TREND/QNA)
    author_id  BIGINT       NOT NULL,                  -- 작성자 (users.id 참조)
    created_at DATETIME     NOT NULL DEFAULT NOW(),    -- 작성일시 (자동 설정)
    updated_at DATETIME     NOT NULL DEFAULT NOW(),    -- 수정일시 (자동 설정)
    PRIMARY KEY (id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
