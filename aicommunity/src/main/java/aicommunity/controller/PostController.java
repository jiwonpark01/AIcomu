package aicommunity.controller;

import aicommunity.domain.Category;
import aicommunity.dto.post.PostCreateRequest;
import aicommunity.dto.post.PostResponse;
import aicommunity.dto.post.PostUpdateRequest;
import aicommunity.security.CustomUserDetails;
import aicommunity.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 루트 → 게시글 목록으로 리다이렉트
    @GetMapping("/")
    public String root() {
        return "redirect:/posts";
    }

    // 게시글 목록
    @GetMapping("/posts")
    public String list(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "posts/list";
    }

    // 게시글 상세
    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "posts/detail";
    }

    // 게시글 작성 폼
    @GetMapping("/posts/new")
    public String createForm(Model model) {
        model.addAttribute("postCreateRequest", new PostCreateRequest());
        model.addAttribute("categories", Category.values());  // 카테고리 목록 전달
        return "posts/form";
    }

    // 게시글 작성 처리
    @PostMapping("/posts/new")
    public String create(@Valid @ModelAttribute PostCreateRequest request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            return "posts/form";
        }

        Long postId = postService.save(request, userDetails.getUser().getId());
        return "redirect:/posts/" + postId;
    }

    // 게시글 수정 폼
    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PostResponse post = postService.findById(id);
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle(post.getTitle());
        request.setContent(post.getContent());
        request.setCategory(post.getCategory());
        model.addAttribute("postId", id);
        model.addAttribute("postUpdateRequest", request);
        model.addAttribute("categories", Category.values());
        return "posts/edit";
    }

    // 게시글 수정 처리
    @PostMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute PostUpdateRequest request,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("categories", Category.values());
            return "posts/edit";
        }

        postService.update(id, request, userDetails.getUser().getId());
        return "redirect:/posts/" + id;
    }

    // 게시글 삭제 처리
    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.delete(id, userDetails.getUser().getId());
        return "redirect:/posts";
    }
}
