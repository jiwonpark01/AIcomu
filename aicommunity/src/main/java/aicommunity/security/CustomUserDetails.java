package aicommunity.security;

import aicommunity.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final User user;  // 원본 User 객체를 그대로 보관

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Spring Security가 권한 체크할 때 사용 — "ROLE_USER", "ROLE_ADMIN" 형태여야 함
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // BCrypt 암호화된 비밀번호
    }

    @Override
    public String getUsername() {
        return user.getUsername();  // 로그인 ID
    }
}
