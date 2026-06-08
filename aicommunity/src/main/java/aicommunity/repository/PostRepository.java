package aicommunity.repository;

import aicommunity.domain.Category;
import aicommunity.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    // ResultSet 한 행을 Post 객체로 변환
    private final RowMapper<Post> postRowMapper = (rs, rowNum) -> {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setCategory(Category.valueOf(rs.getString("category")));
        post.setAuthorId(rs.getLong("author_id"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return post;
    };

    // 새 게시글을 posts 테이블에 삽입하고 생성된 id를 post에 세팅
    public Post save(Post post) {
        String sql = "INSERT INTO posts (title, content, category, author_id, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getCategory().name());
            ps.setLong(4, post.getAuthorId());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        post.setId(keyHolder.getKey().longValue());
        return post;
    }

    // id로 게시글 한 개 조회 — 없으면 Optional.empty()
    public Optional<Post> findById(Long id) {
        String sql = "SELECT id, title, content, category, author_id, created_at, updated_at " +
                     "FROM posts WHERE id = ?";
        return jdbcTemplate.query(sql, postRowMapper, id)
                .stream()
                .findFirst();
    }

    // 전체 게시글 조회 — 최신순(created_at 내림차순) 정렬
    public List<Post> findAll() {
        String sql = "SELECT id, title, content, category, author_id, created_at, updated_at " +
                     "FROM posts ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, postRowMapper);
    }

    // 게시글 수정 — 제목, 본문, 카테고리, 수정일시 업데이트
    public void update(Post post) {
        String sql = "UPDATE posts SET title = ?, content = ?, category = ?, updated_at = ? " +
                     "WHERE id = ?";
        jdbcTemplate.update(sql,
                post.getTitle(),
                post.getContent(),
                post.getCategory().name(),
                Timestamp.valueOf(LocalDateTime.now()),
                post.getId());
    }

    // id로 게시글 삭제
    public void delete(Long id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
