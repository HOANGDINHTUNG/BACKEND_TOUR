# WedService Backend

## 1. Tôi đã chỉnh gì để project chạy ổn hơn

Bản bạn gửi lên mới có thư mục `src`, nên tôi đã hoàn thiện lại project theo hướng có thể mở bằng IntelliJ rồi chạy Maven bình thường.

Các điểm tôi đã xử lý:

- Thêm `pom.xml` để project có thể build/test bằng Maven.
- Thêm `.gitignore` và xóa các file IDE như `.iml`.
- Xóa toàn bộ `readme.md` rải trong source và các comment cũ trong code để source sạch hơn.
- Gom DTO về một chuẩn duy nhất:
  - `module.user.dto.request`
  - `module.user.dto.response`
- Xóa tình trạng trùng class DTO cũ/mới gây dễ lỗi import và lỗi compile.
- Sửa các import Jackson sai từ `tools.jackson...` sang `com.fasterxml.jackson...`.
- Sửa `SecurityConfig` để matcher dùng đúng path thực tế của controller khi có `context-path=/api/v1`.
- Tách rõ trách nhiệm giữa auth và user profile:
  - `AuthController`: register/login
  - `UserController`: CRUD user + `/users/me`
- Bổ sung `UserMapper` vào luồng xử lý chính để map entity -> response nhất quán.
- Bổ sung test theo từng nhóm: service, repository, controller, security, context load.
- Thêm `application-test.yaml` dùng H2 để test không phụ thuộc MySQL local.

## 2. Các lỗi gốc trong bản bạn gửi

Đây là các vấn đề quan trọng khiến project rất dễ không chạy được hoặc chạy nhưng không ổn:

1. **Thiếu file build**  
   Không có `pom.xml`, nên project chưa đủ để Maven build/test.

2. **Bị lẫn 2 bộ DTO user**  
   Bạn đang có cả:
   - `module.user.dto.*`
   - `module.user.dto.request/*` và `module.user.dto.response/*`

   Đây là dấu hiệu đang refactor dở. Khi controller/service vẫn import DTO cũ còn mapper/import nơi khác dùng DTO mới thì rất dễ lệch kiểu dữ liệu.

3. **Sai import ObjectMapper/TypeReference**  
   Một số file đang dùng `tools.jackson...`, cái này sẽ lỗi compile trong project Spring Boot thông thường.

4. **Security matcher dễ sai khi có context-path**  
   Với `server.servlet.context-path=/api/v1`, controller vẫn khai báo `/auth`, `/users`, `/system/...`. Ở lớp security thì matcher nên khớp theo path của handler, không nên trộn cứng `/api/v1/...` như bản cũ.

5. **Code sạch chưa đồng nhất**  
   Có comment học tập cũ, readme rải trong package Java, file `.iml`, class đang dùng dở nhưng chưa nối vào luồng chính.

6. **Phần test gần như chưa có**  
   Mới chỉ có `contextLoads()` nên chưa đủ bảo vệ logic service/controller/security.

## 3. Cấu trúc sau khi dọn lại

```text
wedservice-backend-fixed/
├── pom.xml
├── .gitignore
├── README.md
└── src
    ├── main
    │   ├── java/com/wedservice/backend
    │   └── resources
    └── test
        ├── java/com/wedservice/backend
        └── resources
```

## 4. Cách chạy project

### Yêu cầu

- Java 21 trở lên
- MySQL đã tạo database `wedservice`
- Maven

### Chạy local

```bash
mvn spring-boot:run
```

Hoặc:

```bash
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Test

```bash
mvn test
```

## 5. Cấu hình môi trường

### `application.yaml`
- set app name
- bật profile `dev`
- set port `8080`
- set context path `/api/v1`

### `application-dev.yaml`
- cấu hình MySQL dev
- `ddl-auto=update`
- cấu hình JWT secret/expiration

### `application-test.yaml`
- dùng H2 in-memory để test
- không phụ thuộc MySQL local

## 6. API chính sau khi sửa

### Auth
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### System
- `GET /api/v1/system/health`

### User
- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/deactivate`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`

## 7. Test đã thêm

### Service tests
- `AuthServiceTest`
- `UserServiceTest`

### Security/unit tests
- `JwtServiceTest`

### Repository tests
- `UserRepositoryTest`

### Controller tests
- `AuthControllerTest`
- `UserControllerTest`
- `SystemControllerTest`

### Context test
- `BackendApplicationTests`

## 8. Đánh giá từng file

> Mục này đi theo đúng ý bạn: mỗi file có tác dụng gì, điểm mạnh gì, điểm yếu gì, và nên cải thiện gì tiếp theo.

### 8.1 Root files

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `pom.xml` | Khai báo dependency, plugin build, phiên bản Java/Spring Boot | Đủ dependency cho web, jpa, security, validation, mysql, test, h2 | Chưa tách profile build riêng cho prod; sau này có thể thêm plugin checkstyle/spotbugs/jacoco |
| `.gitignore` | Chặn file build và file IDE không đẩy lên git | Gọn, đủ cho Maven/IntelliJ cơ bản | Có thể thêm `.env`, coverage report nếu sau này dùng |
| `README.md` | Tài liệu tổng hợp toàn project | Gom thông tin sửa lỗi, cách chạy, test, mô tả từng file | Dài; sau này có thể tách thành `docs/architecture.md`, `docs/api.md`, `docs/testing.md` |

### 8.2 Application entry

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `src/main/java/com/wedservice/backend/BackendApplication.java` | Điểm khởi động Spring Boot | Tối giản, đúng chuẩn, dễ bảo trì | Chưa có bootstrap data hay profile-specific startup logging |

### 8.3 Common exception package

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `common/exception/BadRequestException.java` | Ném lỗi 400 cho dữ liệu/luồng nghiệp vụ không hợp lệ | Đơn giản, dễ đọc | Có thể chuẩn hóa constructor nhiều dạng hơn nếu sau này cần error metadata |
| `common/exception/ResourceNotFoundException.java` | Ném lỗi 404 khi không tìm thấy dữ liệu | Thẳng, rõ, đúng ngữ nghĩa | Có thể gắn thêm resource type/id riêng |
| `common/exception/UnauthorizedException.java` | Ném lỗi 401 cho trường hợp chưa xác thực | Gọn | Chưa có constructor mặc định/message chuẩn hệ thống |
| `common/exception/ErrorResponse.java` | DTO chuẩn cho phản hồi lỗi | Format lỗi nhất quán, dễ dùng cho frontend | Chưa có traceId/path để debug production |
| `common/exception/GlobalExceptionHandler.java` | Gom xử lý exception toàn hệ thống | Tách lỗi 400/401/404/500 rõ ràng, có validation map | Chưa xử lý riêng `IllegalArgumentException`, `DataIntegrityViolationException`, lỗi JWT chi tiết |

### 8.4 Common response package

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `common/response/ApiResponse.java` | Wrapper chuẩn cho mọi response thành công | Frontend nhận format thống nhất | Chưa có field `meta` hoặc `code` nếu sau này cần mở rộng |
| `common/response/PageResponse.java` | Chuẩn hóa dữ liệu phân trang trả ra cho client | Không lộ trực tiếp `Page<?>` của Spring | Chưa có sort info; có thể thêm `sort`, `hasNext`, `hasPrevious` |

### 8.5 Common security package

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `common/security/RestAuthenticationEntryPoint.java` | Trả JSON khi chưa đăng nhập mà gọi API bảo vệ | Không bị trả HTML mặc định của Spring Security | Message hiện còn chung, sau này có thể phân biệt token thiếu/token sai/token hết hạn |
| `common/security/RestAccessDeniedHandler.java` | Trả JSON khi đã login nhưng không đủ quyền | Giúp frontend xử lý 403 thống nhất | Chưa có path hoặc permission detail để debug |

### 8.6 Config package

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `config/SecurityConfig.java` | Cấu hình security toàn app | Đã sửa matcher đúng, stateless JWT, phân quyền rõ auth/me/users/admin | Rule vẫn còn khá thủ công; sau này nên tách constants hoặc method riêng để dễ đọc |

### 8.7 Auth module - controller/dto/service

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `module/auth/controller/AuthController.java` | Nhận request register/login | Controller mỏng, chỉ điều phối | Chưa có refresh token / logout |
| `module/auth/dto/RegisterRequest.java` | Input cho đăng ký | Có validation cơ bản, dễ dùng | Chưa có rule mạnh cho password |
| `module/auth/dto/LoginRequest.java` | Input cho đăng nhập | Gọn, đủ | Chưa có remember-me / login by phone |
| `module/auth/dto/AuthResponse.java` | Kết quả trả về sau login/register | Gom user + token + expiration rõ ràng | Chưa có refresh token hoặc scope |
| `module/auth/service/AuthService.java` | Xử lý register/login | Tách rõ nghiệp vụ, normalize email, encode password, tạo token | Chưa tách helper validate reusable; chưa có refresh token và audit login |

### 8.8 Auth module - security

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `module/auth/security/CustomUserDetails.java` | Adapter từ `User` sang `UserDetails` | Gói đủ thông tin userId/fullName/role | Chưa có account locked / expired thật sự |
| `module/auth/security/CustomUserDetailsService.java` | Load user theo email cho Spring Security | Ngắn, đúng vai trò, có normalize email | Chưa cache user; chưa phân tách logic inactive trước khi auth |
| `module/auth/security/JwtProperties.java` | Bind config JWT từ yaml | Đơn giản, dễ đổi bằng env | Nên thêm validation cho secret/expiration |
| `module/auth/security/JwtService.java` | Tạo/đọc/verify JWT thủ công | Chủ động, không phụ thuộc thư viện JWT ngoài, dễ học flow | Tự viết JWT sẽ phải tự chịu trách nhiệm bảo mật; production thường nên dùng thư viện battle-tested |
| `module/auth/security/JwtAuthenticationFilter.java` | Đọc Bearer token ở mỗi request và nạp SecurityContext | Flow rõ, fail-safe bằng clear context | Hiện nuốt exception khá im lặng; production nên log mức debug hoặc phân loại lỗi token |

### 8.9 User module

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `module/user/entity/Role.java` | Enum role hệ thống | Rõ ràng, đủ cho giai đoạn đầu | Chưa phù hợp nếu sau này có nhiều permission chi tiết |
| `module/user/entity/User.java` | Entity ánh xạ bảng `users` | Có unique email, active, role, timestamp tự set | Chưa có soft-delete đầy đủ, chưa có audit actor |
| `module/user/repository/UserRepository.java` | Tầng truy cập DB cho user | Query method gọn, đủ cho CRUD + filter cơ bản | Khi filter phức tạp hơn nên chuyển qua Specification/QueryDSL |
| `module/user/dto/request/CreateUserRequest.java` | Input cho tạo user | Validation đủ dùng | Chưa có regex phone rõ ràng |
| `module/user/dto/request/UpdateUserRequest.java` | Input cho admin update user | Cho phép cập nhật role rõ ràng | Role đang là string; có thể đổi sang enum + custom validator |
| `module/user/dto/request/UpdateMyProfileRequest.java` | Input cho user tự sửa profile | Tách riêng khỏi admin update là đúng hướng | Chưa hỗ trợ đổi password/avatar |
| `module/user/dto/response/UserResponse.java` | Output an toàn cho client | Không lộ password, có timestamp để frontend hiển thị | Nếu API public nhiều, có thể cần nhiều response DTO chuyên biệt hơn |
| `module/user/mapper/UserMapper.java` | Map entity sang response | Giảm lặp code giữa service/auth | Mới có 1 chiều; nếu sau này map phức tạp có thể dùng MapStruct |
| `module/user/service/UserService.java` | Nghiệp vụ user CRUD, profile, filter, deactivate | Bao trọn flow admin + profile, normalize email, parse role, dùng mapper chung | Hơi dài; sau này nên tách `AdminUserService` và `ProfileService` |
| `module/user/controller/UserController.java` | API cho user admin và profile hiện tại | Bao phủ đầy đủ endpoint cơ bản, controller vẫn mỏng | Admin API và self-profile API đang chung controller, sau này có thể tách 2 controller |

### 8.10 System module

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `module/system/controller/SystemController.java` | API health check | Rất hữu ích để kiểm tra app sống | Mới là health logic đơn giản, chưa có DB check/redis/external dependency check |

### 8.11 Resource files

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `src/main/resources/application.yaml` | Cấu hình base toàn app | Có context path rõ, dễ quản lý endpoint | Hiện đang cố định active profile là `dev`; production nên set qua env/runtime |
| `src/main/resources/application-dev.yaml` | Cấu hình môi trường dev | Đủ datasource, JPA, JWT, timezone | Secret mặc định chỉ nên dùng local; production phải bắt buộc truyền env |

### 8.12 Test files

| File | Tác dụng | Điểm mạnh | Điểm yếu / hướng cải thiện |
|---|---|---|---|
| `src/test/resources/application-test.yaml` | Cấu hình test bằng H2 | Giúp test không phụ thuộc MySQL local | Chưa mô phỏng hết behavior MySQL thật |
| `BackendApplicationTests.java` | Kiểm tra context Spring boot được lên | Bắt lỗi wiring cơ bản | Test khá chung, khó chỉ ra lỗi business cụ thể |
| `module/auth/security/JwtServiceTest.java` | Test tạo/đọc/verify JWT | Bọc được logic core của JWT | Chưa test token hết hạn |
| `module/auth/service/AuthServiceTest.java` | Test register/login service | Kiểm tra normalize email, duplicate email, tạo token | Chưa test inactive user/login fail |
| `module/user/service/UserServiceTest.java` | Test nghiệp vụ user | Có test create, invalid role, paging, unauthorized, deactivate | Chưa test filter keyword/active đầy đủ |
| `module/user/repository/UserRepositoryTest.java` | Test query repository với H2 | Có kiểm tra ignore case | Chưa test các method tìm kiếm còn lại |
| `module/auth/controller/AuthControllerTest.java` | Test contract HTTP của auth controller | Kiểm tra response wrapper rõ ràng | Chưa test validation fail |
| `module/user/controller/UserControllerTest.java` | Test contract HTTP của user controller | Kiểm tra request/response cơ bản | Chưa test các endpoint `/users/me`, update, deactivate |
| `module/system/controller/SystemControllerTest.java` | Test health endpoint | Rất nhanh, dễ bắt lỗi route | Chưa test context path full integration |
| `support/TestWebMvcConfig.java` | Config security đơn giản cho controller test | Giúp controller test tập trung vào HTTP contract | Chỉ phù hợp cho test lớp web, không thay thế integration security test |

## 9. Điểm mạnh tổng thể của project hiện tại

- Đã có phân tầng khá đúng: controller -> service -> repository.
- Đã có DTO, không trả thẳng entity ra ngoài.
- Đã có xử lý exception tập trung.
- Đã có JWT auth cơ bản.
- Đã có phân quyền admin và user profile.
- Đã bắt đầu có test theo nhóm chứ không dồn một cục.
- Đã sạch source hơn nhiều sau khi bỏ comment/readme cũ trong package.

## 10. Điểm còn yếu tổng thể và nên làm tiếp

Đây là phần tôi khuyên bạn làm ở vòng tiếp theo:

1. **Tách service lớn**
   - `UserService` đang gánh cả admin CRUD lẫn self-profile.
   - Sau này nên tách nhỏ để dễ test và dễ đọc.

2. **Dùng thư viện JWT chuẩn production**
   - Bản hiện tại tự triển khai JWT để bạn hiểu flow.
   - Nhưng nếu tiến gần production, nên dùng thư viện battle-tested để giảm rủi ro bảo mật.

3. **Bổ sung integration test thật sự cho security**
   - Hiện chủ yếu là unit/web test.
   - Nên có test request với token thật để chắc chắn rule security đang đúng.

4. **Chuẩn hóa validation nâng cao**
   - Phone nên có regex rõ.
   - Password nên có rule phức tạp hơn.
   - Role có thể chuyển sang enum validation.

5. **Tách package rõ hơn theo use-case**
   - Sau này có thể tách:
     - `user/admin`
     - `user/profile`
     - `auth/token`

6. **Thêm migration**
   - Nếu dự án đi xa hơn, nên chuyển từ `ddl-auto=update` sang Flyway/Liquibase.

## 11. Lưu ý quan trọng khi bạn chạy ở máy mình

Vì file bạn gửi không có sẵn wrapper Maven và môi trường làm việc hiện tại của tôi cũng không có Maven CLI, tôi đã sửa project theo hướng **đủ cấu trúc để bạn chạy local bằng Maven** nhưng tôi **không thể chạy lệnh `mvn test` trực tiếp trong môi trường hiện tại** để xác nhận runtime 100% tại chỗ.

Điều đó có nghĩa là:

- Về mặt cấu trúc và logic compile, tôi đã dọn và nối lại cho đồng nhất.
- Nhưng vòng xác nhận cuối cùng bạn nên chạy ở máy của bạn bằng:

```bash
mvn clean test
mvn spring-boot:run
```

Nếu còn lỗi phát sinh do version dependency hoặc môi trường MySQL local, sửa tiếp sẽ rất nhanh vì nền project lúc này đã sạch và rõ hơn nhiều.
