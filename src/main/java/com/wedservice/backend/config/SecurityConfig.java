package com.wedservice.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.wedservice.backend.common.security.RestAccessDeniedHandler;
import com.wedservice.backend.common.security.RestAuthenticationEntryPoint;
import com.wedservice.backend.module.auth.security.CustomUserDetailsService;
import com.wedservice.backend.module.auth.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

// Cấu hình bảo mật của Spring Security
// endpoint nào được truy cập không cần đăng nhập
// endpoint nào phải đăng nhập
// endpoint nào chỉ admin mới được vào
// hệ thống xác thực user bằng cách nào
// dùng JWT filter ở đâu
// khi bị lỗi 401/403 thì trả response như thế nào

// Nói với Spring rằng class này là class cấu hình.
// Spring sẽ đọc nó để tạo các bean liên quan đến bảo mật.

@Configuration
@RequiredArgsConstructor
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(securityProperties.getWhitelist().toArray(new String[0]))
                        .permitAll()
                        // Public GET endpoints for destinations
                        .requestMatchers(HttpMethod.GET, "/destinations", "/destinations/{uuid}").permitAll()
                        // Any other request must be authenticated (rules will be defined in controllers)
                        .anyRequest().authenticated()
                )
                // Đây là nơi chỉ định cách trả lỗi khi có vấn đề bảo mật.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // 401 Unauthorized
                        .accessDeniedHandler(restAccessDeniedHandler) // 403 Forbidden
                )
                // Là nơi trực tiếp làm việc kiểm tra login
                .authenticationProvider(authenticationProvider())
                // JWT chỉ có tác dụng nếu filter chạy TRƯỚC khi Spring check quyền
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Dao ở đây có thể hiểu là nó làm việc với dữ liệu user lấy từ nơi lưu trữ như
        // DB.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        // Nơi để cấu hình PasswordEncoder (cái dùng để mã hóa mật khẩu)
        provider.setPasswordEncoder(passwordEncoder());
        // Trả cái bộ máy xác thực đó cho Spring quản lý.
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
