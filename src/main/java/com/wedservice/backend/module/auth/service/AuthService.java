package com.wedservice.backend.module.auth.service;

import com.wedservice.backend.common.exception.BadRequestException;
import com.wedservice.backend.common.exception.ResourceNotFoundException;
import com.wedservice.backend.module.auth.dto.AuthResponse;
import com.wedservice.backend.module.auth.dto.LoginRequest;
import com.wedservice.backend.module.auth.dto.RegisterRequest;
import com.wedservice.backend.module.auth.security.CustomUserDetails;
import com.wedservice.backend.module.auth.security.JwtService;
import com.wedservice.backend.module.user.entity.Gender;
import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.Status;
import com.wedservice.backend.module.user.entity.User;
import com.wedservice.backend.module.user.mapper.UserMapper;
import com.wedservice.backend.module.user.repository.UserRepository;
import com.wedservice.backend.module.user.util.UserContactNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*
    Đây là lớp service, hãy tạo object và quản lý nó cho tôi

    Hiểu đơn giản:
        nếu không có @Service, class này chỉ là class Java bình thường
        có @Service, Spring sẽ tạo ra bean AuthService
        các class khác như AuthController có thể dùng nó

    Dễ hiểu hơn
        @Service giống như dán nhãn: “Thằng này là nơi xử lý nghiệp vụ”
 */

@Service
@RequiredArgsConstructor
public class AuthService {

    // Dùng để làm việc với database bảng user
    private final UserRepository userRepository;
    // Dùng để mã hóa password
    private final PasswordEncoder passwordEncoder;
    // Dùng để xác thực đăng nhập (“máy kiểm tra đăng nhập” của Spring Security)
    private final AuthenticationManager authenticationManager;
    // Dùng để tạo JWT token và lấy thời gian hết hạn
    private final JwtService jwtService;
    // Dùng để chuyển từ User entity sang object trả về cho client
    private final UserMapper userMapper;

    public AuthResponse register(RegisterRequest request) {
        String email = UserContactNormalizer.normalizeEmail(request.getEmail());
        String phone = UserContactNormalizer.normalizePhone(request.getPhone());
        validateRequiredContact(email, phone);
        validateUniqueContacts(email, phone, null);

        String fullName = request.getFullName().trim();
        User user = User.builder()
                .fullName(fullName)
                .displayName(StringUtils.hasText(request.getDisplayName()) ? request.getDisplayName().trim() : fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.getPasswordHash()))
                .role(Role.CUSTOMER)
                .status(Status.ACTIVE)
                .gender(request.getGender() == null ? Gender.UNKNOWN : request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .avatarUrl(normalizeNullable(request.getAvatarUrl()))
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        /*
            Đem phiếu đó sang cho Spring Security kiểm tra.

            Spring Security sẽ:
                lấy email
                tìm user tương ứng
                lấy password mã hóa trong DB
                so sánh với password user nhập
                nếu đúng thì xác thực thành công
                nếu sai thì ném exception

            Nếu sai: Nó sẽ không chạy tiếp, mà văng lỗi luôn.

            Nếu đúng: Nó trả về object Authentication.
         */
        String login = UserContactNormalizer.normalizeLoginIdentifier(request.getLogin());

        Authentication authentication = authenticationManager.authenticate(
            /*
                Tạo ra một object chứa thông tin đăng nhập:
                    username ở đây là email
                    password là mật khẩu user nhập

                Nó giống như một “phiếu yêu cầu xác thực”.
                */
            new UsernamePasswordAuthenticationToken(login, request.getPasswordHash())
        );

        // Kiểu Spring Security nó làm việc với UserDetails không phải User
        // Principal là sau khi xác thực thành công, Authentication sẽ giữ thông tin user đăng nhập
        Object principalObj = authentication.getPrincipal();

        // nếu KHÔNG phải CustomUserDetails → throw lỗi
        // nếu đúng → auto cast sang principal
        if (!(principalObj instanceof CustomUserDetails principal)) {
            throw new ResourceNotFoundException("Invalid authentication principal");
        }

        /*
            Mặc dù principal đã có thông tin xác thực, nhưng nhiều hệ thống vẫn tìm lại User đầy đủ từ DB để:
                lấy đúng entity mới nhất
                lấy đủ field cần cho response
                đảm bảo dữ liệu chuẩn
         */
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + principal.getUserId()
                ));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

        return AuthResponse.builder()
                // Chuyển entity User sang DTO trả về.
                .user(userMapper.toResponse(user))
                // Ghi loại token là Bearer (ghi chứ cho frontend biết cách dùng)
                .tokenType("Bearer")
                // Tạo JWT access token: Đây là token client sẽ cầm để gọi các API cần đăng nhập
                .accessToken(jwtService.generateAccessToken(userDetails))
                // Trả thời gian hết hạn token (client có thể biết khi nào token hết hạn để refresh / login lại)
                .expiresIn(jwtService.getExpiration())
                .build();
    }

    private void validateRequiredContact(String email, String phone) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new BadRequestException("At least email or phone must be provided");
        }
    }

    private void validateUniqueContacts(String email, String phone, java.util.UUID currentUserId) {
        if (StringUtils.hasText(email)) {
            boolean emailExists = currentUserId == null
                    ? userRepository.existsByEmailIgnoreCase(email)
                    : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId);
            if (emailExists) {
                throw new BadRequestException("Email already exists");
            }
        }

        if (StringUtils.hasText(phone)) {
            boolean phoneExists = currentUserId == null
                    ? userRepository.existsByPhone(phone)
                    : userRepository.existsByPhoneAndIdNot(phone, currentUserId);
            if (phoneExists) {
                throw new BadRequestException("Phone already exists");
            }
        }
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
