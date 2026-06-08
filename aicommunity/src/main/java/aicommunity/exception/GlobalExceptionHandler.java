package aicommunity.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 권한 없음 — 본인 글이 아닌데 수정/삭제 시도
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorized(UnauthorizedException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/403";
    }

    // 존재하지 않는 게시글 조회/수정/삭제 시도
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }
}
