package aicommunity.service;

import aicommunity.domain.Post;
import aicommunity.domain.User;
import aicommunity.dto.post.PostCreateRequest;
import aicommunity.dto.post.PostResponse;
import aicommunity.dto.post.PostUpdateRequest;
import aicommunity.exception.UnauthorizedException;
import aicommunity.repository.PostRepository;
import aicommunity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 저장 — 로그인한 유저의 id를 author_id로 설정
    @Transactional
    public Long save(PostCreateRequest request, Long authorId) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setAuthorId(authorId);

        return postRepository.save(post).getId();
    }

    // 전체 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponse> findAll() {
        return postRepository.findAll().stream()
                .map(post -> {
                    // 게시글의 author_id로 작성자 닉네임 조회
                    String nickname = userRepository.findById(post.getAuthorId())
                            .map(User::getNickname)
                            .orElse("알 수 없음");
                    return new PostResponse(post, nickname);
                })
                .toList();
    }

    // 게시글 수정 — 본인 글인지 확인 후 수정
    @Transactional
    public void update(Long postId, PostUpdateRequest request, Long loginUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        // 소유권 체크 — 작성자가 아니면 예외 던짐
        if (!post.getAuthorId().equals(loginUserId)) {
            throw new UnauthorizedException("본인이 작성한 글만 수정할 수 있습니다");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        postRepository.update(post);
    }

    // 게시글 삭제 — 본인 글인지 확인 후 삭제
    @Transactional
    public void delete(Long postId, Long loginUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        // 소유권 체크 — 작성자가 아니면 예외 던짐
        if (!post.getAuthorId().equals(loginUserId)) {
            throw new UnauthorizedException("본인이 작성한 글만 삭제할 수 있습니다");
        }

        postRepository.delete(postId);
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다"));

        String nickname = userRepository.findById(post.getAuthorId())
                .map(User::getNickname)
                .orElse("알 수 없음");

        return new PostResponse(post, nickname);
    }
}
