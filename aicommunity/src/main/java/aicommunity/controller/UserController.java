package aicommunity.controller;

import aicommunity.dto.user.SignupRequest;
import aicommunity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 로그인 폼 페이지 보여주기
    @GetMapping("/login")
    public String loginForm() {
        return "user/login";  // templates/user/login.html
    }

    // 회원가입 폼 페이지 보여주기
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "user/signup";  // templates/user/signup.html
    }

    // 회원가입 폼 제출 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request,
                         BindingResult bindingResult) {
        // 검증 오류가 있으면 폼으로 돌아감
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        try {
            userService.signup(request);
        } catch (IllegalArgumentException e) {
            // 아이디 중복 오류를 폼에 표시
            bindingResult.rejectValue("username", "duplicate", e.getMessage());
            return "user/signup";
        }

        return "redirect:/login";  // 가입 성공 시 로그인 페이지로
    }
}
