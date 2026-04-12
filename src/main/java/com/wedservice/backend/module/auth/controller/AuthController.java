package com.wedservice.backend.module.auth.controller;

// Class này thường dùng để chuẩn hóa response trả về cho client.
import com.wedservice.backend.common.response.ApiResponse;
// Đây là kiểu dữ liệu trả về sau khi đăng ký hoặc đăng nhập thành công.
// Ví dụ nó có thể chứa một số thứ như token, refresh token, thông tin user, role, email
import com.wedservice.backend.module.auth.dto.AuthResponse;
// Dùng để nhận dữ liệu khi client gọi API login.
import com.wedservice.backend.module.auth.dto.LoginRequest;
// Dùng để nhận dữ liệu khi client gọi API register.
import com.wedservice.backend.module.auth.dto.RegisterRequest;
/*
    Controller không tự xử lý logic nặng như:
        kiểm tra email tồn tại chưa
        mã hóa mật khẩu
        tạo JWT
        lưu user vào DB
        xác thực mật khẩu
    Mà giao việc đó cho AuthService

    Hiểu đơn giản: Controller là người tiếp tân, còn AuthService là người làm nghiệp vụ thật.
 */
import com.wedservice.backend.module.auth.facade.AuthFacade;
// Dùng để kích hoạt validation cho object request
// // Hiểu đơn giản kiểm tra dữu liệu đầu vào trước khi xử lý.
import jakarta.validation.Valid;
// Tự sinh constructor cho các field final hoặc @NonNull.
import lombok.RequiredArgsConstructor;
// Đánh dấu method xử lý HTTP POST.
import org.springframework.web.bind.annotation.PostMapping;
// Lấy dữ liệu JSON từ body request và chuyển thành object Java.
import org.springframework.web.bind.annotation.RequestBody;
// Định nghĩa đường dẫn gốc cho controller.
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wedservice.backend.module.auth.dto.RefreshTokenRequest;

/*
    nhận request từ client ở các API /auth/register và /auth/login
    lấy dữ liệu người dùng gửi lên
    gọi AuthFacade để xử lý nghiệp vụ
    trả kết quả về dưới dạng ApiResponse<AuthResponse>
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authFacade.register(request);
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Register successfully")
                .data(response)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authFacade.login(request);
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successfully")
                .data(response)
                .build();
    }

    /**
     * Dùng refresh token để lấy cặp token mới (access + refresh).
     * Client gửi refreshToken, nhận về accessToken mới và refreshToken mới.
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authFacade.refresh(request);
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(response)
                .build();
    }
}
