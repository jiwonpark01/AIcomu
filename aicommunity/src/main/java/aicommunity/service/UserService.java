package aicommunity.service;

import aicommunity.domain.Role;
import aicommunity.domain.User;
import aicommunity.dto.user.SignupRequest;
import aicommunity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        // 아이디 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다");
        }

        // DTO → domain 변환 + 비밀번호 암호화
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // 평문 저장 금지
        user.setNickname(request.getNickname());
        user.setRole(Role.USER);

        userRepository.save(user);
    }
}
