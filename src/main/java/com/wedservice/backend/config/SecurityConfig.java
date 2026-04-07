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
import com.wedservice.backend.module.users.entity.Role;

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
// Dùng để tạo contructor cho các field final
@RequiredArgsConstructor
public class SecurityConfig {
    // Đây là filter dùng để đọc JWT token từ request.
    // lấy token ra
    // kiểm tra token có hợp lệ không
    // nếu hợp lệ thì xác định user là ai
    // gắn thông tin user vào SecurityContext
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // Đây là service để Spring Security lấy thông tin user từ database.
    private final CustomUserDetailsService customUserDetailsService;
    // user chưa đăng nhập hoặc token không hợp lệ nhưng lại gọi API cần xác thực
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    // user đã đăng nhập rồi nhưng không đủ quyền để thực thi các thao tác khác
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    // SecurityFilterChain là các chuỗi filter (cổng kiểm tra) chạy trước khi
    // resquest vào controller
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // tắt CSRF
                .formLogin(AbstractHttpConfigurer::disable) // tắt loginform mặc định
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth //// Cấu hình quyền truy cập API
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/system/health")
                        .permitAll() // Các enpoint này không cần đăng nhập có thể dô trực tiếp
                        // Destination public endpoints - xem danh sách & chi tiết điểm đến
                        .requestMatchers(HttpMethod.GET, "/destinations", "/destinations/{uuid}").permitAll()
                        // Destination follow - cần đăng nhập
                        .requestMatchers("/destinations/me/**").authenticated()
                        .requestMatchers("/destinations/*/follow", "/destinations/*/follow/**").authenticated()
                        .requestMatchers("/destinations/propose").authenticated()
                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/users/me", "/users/me/**").authenticated() // Cần phải đăng nhập hợp lệ và có
                                                                                      // token hợp lệ
                        .requestMatchers("/users/**").hasRole(Role.ADMIN.name()) // lưu ý cái so sánh của nó là
                                                                                 // ROLE_ADMIN nhma .hasRole() có cơ chế
                                                                                 // chuyển rồi nên ko ảnh hưởng
                        .anyRequest().authenticated() // mọi api khác không khớp thì vẫn phải đăng nhập
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
