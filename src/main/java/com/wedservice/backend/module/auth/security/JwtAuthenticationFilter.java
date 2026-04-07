package com.wedservice.backend.module.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
    Đây là một filter kiểm tra JWT.

    Nói cực dễ hiểu:
        mỗi khi client gửi request lên server
        filter này sẽ đứng giữa đường để kiểm tra:
            có token không
            token có đúng không
            token đó thuộc về ai

    Nếu đúng, nó sẽ đánh dấu request này là đã đăng nhập
    rồi mới cho request đi tiếp vào controller
 */

// Hãy tạo object này và quản lý nó như một bean

@Component
@RequiredArgsConstructor
// Kế thừa class OncePerRequestFilter để mỗi lần request chỉ chạy đùng 1 lần cho dùng nó có forward bao nhiêu lần đi nữa
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        // Kiểm tra repuest có mang token không và có đúng định dạng hay không
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Chỉ lấy token phía sau(bỏ Bearer)
        String token = authorizationHeader.substring(7);

        try {
            // Giải mã token lấy sub(email)
            String email = jwtService.extractSubject(token);

            // Kiểm tra chưa có ai đăng nhập trong Context và token giải mã có hợp lệ hay không
            if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {
                    // Tạo một đối tượng authentication
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // principal: người đăng nhập
                            null, // credentials: thông tin người dùng để chứng minh danh tính, vì đã dùng jwt nên để null
                            userDetails.getAuthorities() // authorities: Danh sách quyền Admin
                    );
                    // Dòng này thêm một số thông tin phụ của request vào token hiện tại
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Nạp vào tất cả vào context và từ thời đểm này hãy coi request này đã đăng nhập với user và quyền này
                    // JWT filter phải set Authentication vào SecurityContext.
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception ex) {
            // Xoá sạch thông tin xác thực hiện tại, coi như request này chưa đăng nhập.
            SecurityContextHolder.clearContext();
        }
        // Filter của tao xong rồi, giờ cho request đi tiếp qua các filter khác rồi tới controller.
        // request sẽ bị dừng tại filter
        // client cứ treo hoặc không tới được controller
        filterChain.doFilter(request, response);
    }
}
