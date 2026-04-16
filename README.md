# TravelViet Backend Tour

Backend cho hệ thống đặt tour / quản lý nội dung du lịch của TravelViet.

README này không đi sâu giải thích logic từng hàm, mà tập trung mô tả:

- dự án được xây theo hướng nào
- dùng framework / thư viện gì
- cấu trúc mã nguồn ra sao
- các quyết định kỹ thuật quan trọng
- các chi tiết nhỏ nhưng đáng giá trong cách tổ chức code
- hiện trạng mạnh, yếu, điểm cần lưu ý khi tiếp tục phát triển

---

## 1. Tổng Quan Kỹ Thuật

### 1.1 Mục tiêu của backend

Dự án hiện tại hướng đến một backend dạng nghiệp vụ cho du lịch với các nhóm chức năng:

- xác thực người dùng
- quản lý người dùng và phân quyền
- quản lý destination
- quản lý tour
- booking
- payment / refund
- review
- health check và quan sát hệ thống

### 1.2 Kiểu kiến trúc đang dùng

Codebase đang đi theo hướng chia lớp rõ ràng:

- `controller`: nhận request, khai báo endpoint, đặt `@PreAuthorize`
- `facade`: lớp điều phối ngắn giữa controller và service
- `service`: xử lý nghiệp vụ
- `repository`: truy cập dữ liệu
- `dto`: request/response contract
- `entity`: mô hình JPA
- `mapper`: map entity <-> dto bằng MapStruct
- `validator`: giữ các rule validation nghiệp vụ riêng
- `common`: phần dùng chung toàn hệ thống

Điểm đáng chú ý là dự án không gom tất cả vào một service lớn, mà đang tách dần theo flow:

- `service/command`
- `service/query`
- `service/impl`

Mức độ đồng đều giữa các module chưa hoàn toàn giống nhau, nhưng xu hướng kiến trúc là rõ.

### 1.3 Triết lý kỹ thuật đang thể hiện qua code

- ưu tiên Spring MVC truyền thống, không dùng reactive
- ưu tiên RBAC + permission chi tiết thay vì chỉ role cứng
- ưu tiên DTO rõ ràng thay vì trả entity trực tiếp
- ưu tiên mapper riêng thay vì map tay rải rác
- ưu tiên filter / utility / common layer cho các concern dùng chung
- ưu tiên test theo nhóm: controller, security, service, repository, integration

---

## 2. Stack, Framework Và Thư Viện Đang Dùng

### 2.1 Nền tảng chính

| Thành phần | Giá trị hiện tại |
| --- | --- |
| Java | `21` |
| Spring Boot | `4.0.5` |
| Build tool | Maven |
| Web stack | Spring Web MVC |
| ORM | Spring Data JPA + Hibernate |
| Database dev | MySQL |
| Database test | H2 |

### 2.2 Các starter và thư viện chính trong `pom.xml`

| Nhóm | Thư viện |
| --- | --- |
| Web API | `spring-boot-starter-webmvc` |
| Security | `spring-boot-starter-security` |
| Validation | `spring-boot-starter-validation` |
| Persistence | `spring-boot-starter-data-jpa` |
| Monitoring | `spring-boot-starter-actuator` |
| Cache | `spring-boot-starter-cache`, `caffeine` |
| Migration | `flyway-mysql` |
| Mapping | `mapstruct` |
| Dynamic query | `querydsl-jpa`, `querydsl-apt` |
| Rate limit | `bucket4j-core` |
| DB driver | `mysql-connector-j` |
| Test DB | `h2` |
| Test support | `spring-boot-starter-*-test` |
| Boilerplate reduction | `lombok` |

### 2.3 Điểm hay trong lựa chọn stack

- chọn `spring-boot-starter-webmvc` thay vì một stack nặng hoặc quá rộng
- có `validation`, `security`, `jpa` ngay từ đầu nên nền khá chắc
- dùng `MapStruct` cho mapping thay vì lạm dụng map tay
- dùng `QueryDSL` cho filter động thay vì viết query string thủ công
- có `Actuator` để observability
- có `Bucket4j` cho rate limit auth endpoints
- có `Caffeine` cho cache nội bộ
- có `Flyway` để quản lý schema bằng migration

### 2.4 Điểm rất đáng chú ý

Dự án này **không đi theo kiểu "crud demo tối giản"**.
Nó đã chứa nhiều mảnh ghép thường thấy ở backend thật:

- permission-based authorization
- cache
- audit timestamps
- rate limit
- correlation id cho log
- stored procedure cho quote refund
- test profile H2

---

## 3. Cách Dự Án Được Tổ Chức

### 3.1 Cấu trúc thư mục mức cao

```text
BACKEND_TOUR/
├── AGENTS.md
├── API_DOCUMENTATION.md
├── ERD.sql
├── PROJECT_MEMORY.md
├── README.md
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/wedservice/backend/
│   │   │   ├── common/
│   │   │   ├── config/
│   │   │   └── module/
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       └── db/migration/
│   └── test/
│       ├── java/com/wedservice/backend/
│       └── resources/application-test.yaml
└── logs/
```

### 3.2 Ý nghĩa các package chính

#### `common`

Chứa phần nền dùng chung:

- `controller`
- `entity`
- `exception`
- `logging`
- `mapper`
- `response`
- `security`
- `service`
- `util`

Đây là dấu hiệu tốt vì dự án có một lớp shared foundation tương đối rõ.

#### `config`

Hiện có:

- `SecurityConfig`
- `SecurityProperties`

Phần config đang khá tập trung, chưa bị rải khắp repo.

#### `module`

Chứa nghiệp vụ chính:

- `auth`
- `users`
- `destinations`
- `tours`
- `bookings`
- `payments`
- `reviews`
- `system`

### 3.3 Kiểu tổ chức bên trong module

Phần lớn module đi theo pattern này:

```text
module/<name>/
├── controller/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── facade/
├── mapper/
├── repository/
├── service/
│   ├── command/
│   ├── query/
│   └── impl/
└── validator/
```

Điểm này rất quan trọng vì nó cho thấy dự án đang hướng tới:

- tách input/output khỏi persistence model
- tách read/write flow
- giảm coupling giữa controller với service chi tiết

### 3.4 Đánh giá về cấu trúc

**Điểm mạnh**

- rõ module
- rõ layer
- dễ mở rộng thêm API theo module
- dễ test theo tầng
- dễ onboarding hơn một codebase gom tất cả vào `service` / `controller`

**Điểm cần lưu ý**

- các facade nghiệp vụ chính đã chuyển sang phụ thuộc `command/query` thay vì gọi trực tiếp các service kiểu cũ
- các lớp `Service` trung gian chỉ dùng để chuyển tiếp đã được dọn bớt để giảm chồng chéo kiến trúc

---

## 4. Điểm Khởi Động Và Năng Lực Nền

File khởi động: `BackendApplication.java`

Project hiện bật:

- `@SpringBootApplication`
- `@EnableJpaAuditing`
- `@EnableCaching`
- `@EnableAsync`

### Ý nghĩa thực tế

- `@EnableJpaAuditing`: dùng được `createdAt`, `updatedAt` ở entity base
- `@EnableCaching`: destination / tour query có thể cache
- `@EnableAsync`: dự án đã sẵn nền cho các tác vụ bất đồng bộ nếu cần mở rộng sau này

### Đánh giá

Đây là một dấu hiệu tốt vì nền hệ thống được chuẩn bị không chỉ để “chạy được”, mà để phục vụ hệ thống thật:

- theo dõi thời gian tạo / cập nhật
- tối ưu truy vấn hot
- chuẩn bị cho tác vụ nền

---

## 5. Cấu Hình Runtime Và Môi Trường

### 5.1 `application.yaml`

Các cấu hình nền đáng chú ý:

- application name: `wedservice-backend`
- profile mặc định: `dev`
- flyway enabled
- actuator health/info/metrics/prometheus
- log pattern có `traceId`
- port: `8088`
- context-path: `/api/v1`

### 5.2 `application-dev.yaml`

Các điểm rất đáng chú ý:

- datasource dev trỏ vào MySQL local
- username/password lấy qua env:
  - `DB_USERNAME`
  - `DB_PASSWORD`
- default hiện tại:
  - `wed_app_user`
  - `123456`
- schema runtime hiện tại: `wedservice`
- timezone: `Asia/Ho_Chi_Minh`
- file log: `logs/backend.log`
- SQL debug bật ở dev
- Hibernate không tự tạo/sửa bảng ở môi trường dev (`ddl-auto=none`)
- Flyway là nguồn quản lý schema cho các môi trường thật

### 5.3 `application-test.yaml`

Test profile đang dùng:

- H2 in-memory
- mode MySQL
- `ddl-auto=create-drop`
- không phụ thuộc MySQL local

### 5.4 Quyết định kỹ thuật nhỏ nhưng tốt: không dùng root DB trực tiếp

Đây là một chi tiết rất đáng ghi nhận trong cách xây dựng:

- runtime config không mặc định đăng nhập database bằng `root`
- thay vào đó dùng user riêng của ứng dụng qua:
  - `DB_USERNAME`
  - `DB_PASSWORD`

### Vì sao lựa chọn này tốt

- giảm rủi ro bảo mật
- tách quyền vận hành ứng dụng khỏi quyền quản trị database
- dễ giới hạn quyền cho app user
- thuận lợi khi deploy sang server thật
- đúng tinh thần least privilege

### Khuyến nghị thực tế cho môi trường dev/prod

Nên duy trì mô hình:

- 1 user quản trị DB để tạo schema / cấp quyền
- 1 user ứng dụng chỉ có quyền cần thiết để app chạy

Ví dụ README nên xem đây là một chủ đích kỹ thuật của dự án, không phải chi tiết phụ.

### 5.5 Điểm cần lưu ý về tên database

Hiện tại runtime dev đang dùng schema `wedservice`.

Migration `V1__init_schema.sql` đã được chỉnh để chạy trên đúng schema mà datasource/Flyway kết nối vào, thay vì tự `CREATE DATABASE` và `USE` một database khác.

`ERD.sql` vẫn đang dùng tên `travelviet`, nên đây vẫn là tài liệu cần đồng bộ tiếp nếu muốn tên schema trong toàn repo hoàn toàn thống nhất.

---

## 6. Database, Schema Và Cách Mô Hình Hóa Dữ Liệu

### 6.1 Cách tiếp cận dữ liệu

Project không chỉ có bảng tối thiểu cho auth, mà đang mô hình domain khá rộng:

- users
- roles
- permissions
- user_roles
- user_preferences
- user_devices
- user_addresses
- destinations và các bảng con
- tours và các bảng con
- bookings
- payments
- refund_requests
- reviews

### 6.2 Điều này nói lên điều gì

- dự án đang được xây theo hướng hệ thống thực tế, không phải demo CRUD đơn giản
- domain model đã nghĩ tới:
  - phân quyền
  - thói quen người dùng
  - thiết bị
  - địa chỉ
  - nội dung điểm đến
  - lịch khởi hành / pickup / guide
  - thanh toán / hoàn tiền / review

### 6.3 Một số quyết định mô hình hóa đáng chú ý

- dùng UUID cho user
- role và permission tách bảng riêng
- user có nhiều role qua `user_roles`
- dùng nhiều enum ở mức schema để khóa chặt dữ liệu
- có soft-delete timestamp `deleted_at`
- có các cột audit thời gian đầy đủ

### 6.4 Stored procedure

Refund flow hiện đang dùng stored procedure:

- `sp_get_refund_quote`

Điều này cho thấy một phần nghiệp vụ pricing / refund policy được đẩy xuống DB layer.

### 6.5 Đánh giá

**Điểm mạnh**

- schema đủ giàu để đi tiếp lâu dài
- role/permission model làm nền tốt cho backoffice
- domain chi tiết, không bị “mỏng”

**Điểm cần chú ý**

- khi schema rộng như vậy, README phải rất rõ để người mới không lạc
- migration và runtime config phải được giữ đồng bộ chặt

---

## 7. Security Và Kiểm Soát Truy Cập

### 7.1 Security model đang dùng

Project đang dùng:

- stateless JWT
- permission-based authorization
- method security qua `@PreAuthorize`
- custom authentication entry point / access denied handler

### 7.2 `SecurityConfig` hiện đang làm gì

- tắt CSRF
- tắt form login
- tắt basic auth
- dùng session stateless
- whitelist một số endpoint public
- public GET cho destination public
- còn lại yêu cầu authenticated
- quyền chi tiết đặt ở controller qua `@PreAuthorize`

### 7.3 Đây là một lựa chọn tốt vì

- controller nào cần quyền gì nhìn thấy ngay ở endpoint đó
- tránh dồn toàn bộ rule vào một file security quá lớn
- dễ kiểm tra blast radius khi thêm API mới

### 7.4 JWT được triển khai thế nào

Điểm đặc biệt của dự án này:

- JWT đang được tự triển khai bằng HMAC SHA-256
- không phụ thuộc một thư viện JWT chuyên dụng bên ngoài

### Điều này thể hiện gì

- người xây dự án muốn kiểm soát rõ cấu trúc token
- hiểu được flow header / payload / signature
- có thể dễ custom claim

### Đồng thời cũng cần nhớ

- tự triển khai JWT đòi hỏi kỷ luật kiểm thử và review bảo mật tốt hơn
- với production lớn, đây là khu vực cần được giữ rất chặt

### 7.5 `CustomUserDetails`

Project không chỉ nạp một role string đơn lẻ, mà đang nạp:

- `ROLE_<code>`
- toàn bộ permission từ role

Điểm này rất quan trọng vì:

- role dùng để nhóm
- permission mới là thứ authorize trực tiếp nhiều API

### 7.6 `AuthenticatedUserProvider`

Đây là một utility có giá trị thực tế cao trong codebase:

- gom logic lấy current user từ `SecurityContextHolder`
- phân biệt admin / backoffice
- tránh lặp code lấy user hiện tại ở nhiều service

### 7.7 Các lớp bảo vệ phụ thêm

#### Correlation ID

`CorrelationIdFilter`:

- đọc hoặc tạo `X-Request-ID`
- nhét vào MDC với key `traceId`
- trả lại header đó ở response

Đây là chi tiết nhỏ nhưng rất tốt cho debug và tracing.

#### Rate limit

`RateLimitFilter`:

- áp dụng cho `/auth/login` và `/auth/register`
- giới hạn theo IP
- hiện tại là in-memory
- dùng `Bucket4j`

Đây là dấu hiệu của tư duy production-minded.

### 7.8 Access control của dự án hiện tại

Không nên mô tả dự án theo kiểu:

- API này là USER
- API kia là ADMIN

Mà nên mô tả theo:

- `booking.create`
- `payment.view`
- `review.moderate`
- `destination.review`

vì code thực tế authorize theo authority.

---

## 8. Mapping, Querying, Utility Và Dùng Chung

### 8.1 MapStruct

Project dùng MapStruct qua:

- `BaseMapper`
- các mapper cụ thể:
  - `UserMapper`
  - `BookingMapper`
  - `PaymentMapper`
  - `TourMapper`
  - `DestinationMapper`
  - `AuthMapper`

### Lợi ích thực tế

- giảm lặp code map entity -> response
- giữ mapper tập trung
- dễ review data contract
- tránh controller / service phải map tay quá nhiều

### 8.2 `UserMapper` là một ví dụ đáng chú ý

Mapper không chỉ map field đơn giản, mà còn:

- normalize dữ liệu
- set default value
- map primary role và toàn bộ roles
- hỗ trợ cả create/update/profile update

Điều này cho thấy mapper đang được dùng như một lớp chuyển đổi thực thụ, không chỉ là trang trí.

### 8.3 QueryDSL

Project đã đưa QueryDSL vào nhiều chỗ:

- `UserRepository`
- `DestinationRepository`
- `TourRepository`
- service filter động cho user / destination / tour

Đây là một lựa chọn tốt hơn nhiều so với:

- if-else query string thủ công
- hoặc explosion của method name query trong repository

### 8.4 Utility dùng chung

Các utility đáng chú ý:

- `DataNormalizer`
- `SlugUtils`
- `CurrencyUtils`
- `DateUtils`

Ý nghĩa:

- chuẩn hóa dữ liệu đầu vào
- tránh logic normalize bị rải trong nhiều service
- giúp code thống nhất hơn

### 8.5 Base classes

#### `AuditableEntity`

Các entity chính kế thừa `AuditableEntity`, nên có:

- `createdAt`
- `updatedAt`
- `deletedAt`

#### `BaseService`

Đã có nền service base để dùng cho các thao tác CRUD chuẩn hóa.

---

## 9. Cache, Observability Và Operational Readiness

### 9.1 Cache

Project bật cache và hiện đang dùng Caffeine.

Các luồng đã có cache:

- search destination public
- destination detail public
- search tours
- tour detail

### Đây là một quyết định tốt vì

- destination và tour là dữ liệu đọc nhiều
- phù hợp với read-heavy endpoint public
- giảm tải query lặp lại

### 9.2 Observability

Project đã có:

- Actuator
- health/info/metrics/prometheus
- trace id trong log
- file log riêng `logs/backend.log`

### 9.3 Logging

Pattern log đã có:

- timestamp
- thread
- level
- logger
- traceId từ MDC

Chi tiết này giúp backend trông “vào việc” hơn nhiều so với log mặc định.

---

## 10. Cách Chia Nghiệp Vụ Theo Module

### 10.1 Auth

Module auth hiện có:

- controller
- dto
- facade
- mapper
- security
- validator
- service command/query

Điều này cho thấy auth không bị xem là phần phụ, mà được tách khá rõ.

### 10.2 Users

Module users đang khá giàu:

- entity role / permission / user / user-role
- service admin và profile tách riêng
- repository có QueryDSL
- mapper khá đầy đủ
- có test service, controller, repository

### 10.3 Destinations

Đây là một module đậm domain:

- destination public
- destination admin
- proposal flow
- follow flow
- content con: media, food, specialty, activity, tip, event

Đây là module có độ sâu nghiệp vụ tốt và thể hiện rõ ý tưởng sản phẩm.

### 10.4 Tours

Tours không chỉ là một bảng `tour`, mà domain đã nghĩ tới:

- media
- itinerary day / item
- checklist
- seasonality
- schedule
- pickup point
- guide

Điều này rất đáng cho README vì nó cho thấy dự án có chiều sâu dữ liệu.

### 10.5 Bookings

Booking flow hiện có:

- create booking
- get booking
- passenger list riêng

Booking đang là điểm nối giữa:

- user
- tour
- schedule
- payment
- refund
- review

### 10.6 Payments / Refunds

Payment module có:

- payment record
- refund request
- refund approval flow
- quote từ stored procedure

Đây là điểm cho thấy hệ thống không chỉ ghi nhận tiền, mà đã có tư duy policy flow.

### 10.7 Reviews

Review module đã có:

- create review
- get review
- list review theo tour
- list review của tôi
- reply review
- moderation sentiment

Không chỉ là bảng comment đơn giản.

---

## 11. Các Chi Tiết Nhỏ Nhưng Đáng Giá Trong Cách Xây Dựng

Đây là phần rất nên xuất hiện trong README vì nó phản ánh “chất kỹ thuật” của dự án:

### 11.1 Không mặc định dùng root để app truy cập database

- app dùng `DB_USERNAME` / `DB_PASSWORD`
- default local là `wed_app_user`
- đây là thói quen tốt về bảo mật và vận hành

### 11.2 Có correlation id ngay từ đầu

- giúp debug request chain dễ hơn
- tốt cho log aggregation

### 11.3 Có rate limit cho auth

- tránh brute-force quá thô ở login/register

### 11.4 Có base response riêng

- frontend nhận format đồng nhất

### 11.5 Có test profile H2

- không phụ thuộc MySQL local để chạy test

### 11.6 Có soft-delete timestamp

- không phải chỗ nào cũng đã khai thác hết
- nhưng nền dữ liệu đã nghĩ tới vòng đời record

### 11.7 Có permission granularity khá tốt

Ví dụ:

- `destination.review`
- `destination.publish`
- `refund.process`
- `review.moderate`

Đây là mức chia quyền tốt hơn nhiều so với role coarse-grained.

### 11.8 Có cache đúng nơi

- destination public
- tour public

Không bật cache bừa toàn hệ thống.

### 11.9 Có command/query split trong nhiều module

Đây là dấu hiệu dự án đang đi theo một hướng có chủ đích, không phải code tăng trưởng vô tổ chức.

---

## 12. Test Landscape

### 12.1 Nhóm test hiện có

Các test đang thấy trong dự án:

- `BackendApplicationTests`
- `CorrelationIdFilterTest`
- `AuthenticatedUserProviderTest`
- `RateLimitFilterTest`
- `SecurityIntegrationTest`
- `AuthControllerTest`
- `JwtServiceTest`
- `AdminDestinationUpdateIntegrationTest`
- `DestinationProposalIntegrationTest`
- `AdminUserControllerTest`
- `UserProfileControllerTest`
- `UserRepositoryTest`
- `AdminUserServiceTest`
- `UserProfileServiceTest`
- `TourCommandServiceImplTest`
- `BookingCommandServiceImplTest`
- `PaymentCommandServiceImplTest`
- `ReviewServiceImplTest`

### 12.2 Điều này nói lên gì

- test không chỉ có context load
- đã test cả security utility
- đã test cả filter
- đã có integration test cho một số flow destination
- đã có controller test và service test cho users/auth

### 12.3 Đánh giá

**Điểm mạnh**

- nền test tốt hơn nhiều dự án nội bộ nhỏ
- test chạm được nhiều tầng

**Điểm cần phát triển tiếp**

- booking / payment / refund / review nên có thêm integration test riêng
- các flow permission nên có thêm test phủ rộng hơn
- các rule domain quan trọng nên có test regression riêng

### 12.4 Trạng thái sau Phase 0

Sau đợt ổn định nền gần nhất, repo đã có thêm một baseline rõ ràng để đi tiếp Phase 1:

- có `PHASE_0_AUDIT.md` để theo dõi coverage giữa `ERD.sql` và backend hiện tại
- có `PHASE_0_IMPLEMENTATION_CHECKLIST.md` để bám theo từng bảng với nhãn `implemented`, `mvp`, `schema-only`
- các lifecycle status quan trọng đã được chuẩn hóa bằng enum/converter ở `booking`, `payment`, `refund`, `tour`, `tour_schedule`
- đã có thêm service-level regression test cho các flow đang active: `tours`, `bookings`, `payments`, `reviews`
- khi chạy test với Java 25 hiện vẫn có warning từ Lombok `Unsafe` và Mockito dynamic agent; chưa chặn build nhưng nên xử lý ở một lượt kỹ thuật riêng

---

## 13. Đánh Giá Hiện Trạng Codebase

### 13.1 Điểm mạnh nổi bật

1. Domain model có chiều sâu
2. Security model tốt hơn mức CRUD cơ bản
3. Có caching, rate limiting, tracing
4. Có nền test thật sự
5. Có separation of concerns rõ
6. Có utility / mapper / response / exception layer đủ sạch
7. Có tài liệu API riêng và file memory riêng

### 13.2 Điểm cần lưu ý

1. Runtime DB name và migration DB name đang chưa thống nhất
2. Kiến trúc giữa các module chưa đồng đều hoàn toàn
3. Một số comment / file cũ vẫn còn dấu vết encoding hoặc phong cách học tập
4. Có khu vực quan trọng như JWT cần giữ review rất chặt nếu tiếp tục dùng custom implementation

### 13.3 Đánh giá tổng thể

Đây là một codebase **đã vượt khỏi mức project học CRUD thông thường**.
Nó đã có nhiều quyết định mang tính hệ thống thật:

- permission model
- migration
- cache
- filter cross-cutting
- observability
- test profile
- module hóa theo domain

Nếu tiếp tục phát triển có kỷ luật, dự án này đủ nền để mở rộng lâu dài.

---

## 14. Cách Chạy Dự Án

### 14.1 Yêu cầu

- Java 21
- MySQL local
- Maven wrapper

### 14.2 Ghi chú theo môi trường

- Trên Windows, ưu tiên dùng `mvnw.cmd`
- Maven wrapper đã được chỉnh lại để chạy ổn trên môi trường Windows hiện tại
- Nếu cần tách Maven local khỏi user home mặc định, có thể set `MAVEN_USER_HOME` vào thư mục `.m2` trong workspace

### 14.3 Chạy local

```powershell
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### 14.4 Build

```powershell
./mvnw clean install
```

Windows:

```powershell
.\mvnw.cmd clean install
```

### 14.5 Test

```powershell
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

---

## 15. Tài Liệu Liên Quan

- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
- [PROJECT_MEMORY.md](./PROJECT_MEMORY.md)
- [PHASE_0_AUDIT.md](./PHASE_0_AUDIT.md)
- [PHASE_0_IMPLEMENTATION_CHECKLIST.md](./PHASE_0_IMPLEMENTATION_CHECKLIST.md)
- [ERD.sql](./ERD.sql)
- [AGENTS.md](./AGENTS.md)

---

## 16. Ghi Chú Duy Trì README

README này nên được cập nhật khi có thay đổi lớn ở các nhóm sau:

- framework / library
- security model
- database / migration
- cấu trúc module
- test strategy
- phase audit / implementation checklist
- guideline vận hành

README không cần mô tả logic từng hàm.
README nên giữ vai trò là hồ sơ kỹ thuật cấp dự án:

- cách xây
- dùng gì
- vì sao tổ chức như hiện tại
- hiện trạng tốt / cần lưu ý của codebase
