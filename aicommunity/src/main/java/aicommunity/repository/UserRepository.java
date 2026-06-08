package aicommunity.repository;

import aicommunity.domain.Role;
import aicommunity.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    // ResultSet(DB 결과)의 한 행을 User 객체로 변환하는 매퍼
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setRole(Role.valueOf(rs.getString("role")));
        return user;
    };

    // 새 유저를 users 테이블에 삽입하고, DB가 생성한 id를 user에 세팅
    public User save(User user) {
        String sql = "INSERT INTO users (username, password, nickname, role) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setString(4, user.getRole().name());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    // id로 유저 한 명 조회 — 작성자 닉네임 표시에 사용
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, username, password, nickname, role FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, userRowMapper, id)
                .stream()
                .findFirst();
    }

    // username으로 유저 한 명 조회 — 없으면 Optional.empty() 반환
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, nickname, role FROM users WHERE username = ?";
        return jdbcTemplate.query(sql, userRowMapper, username)
                .stream()
                .findFirst();
    }

    // username이 이미 존재하는지 확인 — 회원가입 중복 체크용
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }
}
