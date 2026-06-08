package aicommunity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Cloudflare 터널 환경에서 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()  // 정적 파일은 항상 허용
                .requestMatchers("/signup", "/login", "/error").permitAll()  // 비로그인도 접근 가능
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/posts", "/posts/**").permitAll()  // 게시글 조회는 비로그인도 가능
                .anyRequest().authenticated()                      // 나머지는 로그인 필요
            )
            .formLogin(form -> form
                .loginPage("/login")          // 우리가 만들 로그인 폼 URL
                .defaultSuccessUrl("/posts")  // 로그인 성공 시 이동할 URL
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")   // 로그아웃 후 이동할 URL
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호 암호화 방식
    }
}
