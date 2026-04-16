# Bộ Nhớ Dự Án - BACKEND_TOUR

> Mục đích của file này là giúp một agent mới nắm nhanh bối cảnh dự án mà không phải đọc lại toàn bộ codebase từ đầu.
> Khi bắt đầu một phiên làm việc mới, nên đọc file này trước, sau đó chỉ mở thêm đúng các file liên quan đến yêu cầu hiện tại.
> File này không thay thế việc kiểm tra source thật trước khi sửa code. Nó là lớp tóm tắt để tăng tốc khởi động và giữ ngữ cảnh dài hạn.

---

## 1. Dự Án Này Là Gì

- Tên repo: `BACKEND_TOUR`
- Domain: backend hệ thống đặt tour / du lịch cho TravelViet
- Stack chính:
  - Java Spring Boot
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - MySQL
  - Flyway
- Kiểu kiến trúc trong code hiện tại:
  - `controller`
  - `facade`
  - `service`
  - `repository`
  - `dto`
  - `entity`
- Mục tiêu triển khai:
  - API rõ ràng, có phân quyền chi tiết theo permission
  - dữ liệu domain tách module theo nghiệp vụ
  - tài liệu API phải bám sát source code thật

---

## 2. Cách Chạy Và Cấu Hình Quan Trọng

- Base URL hiện tại: `http://localhost:8088/api/v1`
- Cấu hình lấy từ:
  - `src/main/resources/application.yaml`
  - `src/main/resources/application-dev.yaml`
- Port hiện tại: `8088`
- Context path: `/api/v1`
- Profile mặc định: `dev`
- Runtime dev hiện trỏ MySQL schema `wedservice` qua port `3308`
- Dev không còn để Hibernate tự sửa schema; hướng chuẩn hiện tại là để Flyway quản lý migration
- Lệnh chạy thường dùng:

```powershell
./mvnw spring-boot:run
```

- Lệnh build:

```powershell
./mvnw clean install
```

### Ghi nhớ vận hành

- Đã từng có lỗi cổng `8088` bị chiếm bởi tiến trình Java cũ.
- Nếu app không lên vì port bận, ưu tiên kiểm tra process trước khi đổi config.

---

## 3. Các Module Chính

Các module nằm dưới `src/main/java/com/wedservice/backend/module`:

- `auth`
- `bookings`
- `destinations`
- `payments`
- `reviews`
- `system`
- `tours`
- `users`

### Ý nghĩa ngắn gọn từng module

- `auth`: đăng ký, đăng nhập, refresh token, JWT
- `users`: hồ sơ cá nhân và quản trị người dùng
- `destinations`: destination public, đề xuất destination, admin duyệt destination, follow destination
- `tours`: danh sách tour public và admin CRUD tour
- `bookings`: tạo booking, xem booking
- `payments`: tạo payment, xem payment, refund
- `reviews`: tạo review, xem review, phản hồi review, moderation
- `system`: health check

---

## 4. Kiến Trúc Và Luồng Suy Nghĩ Nên Dùng Khi Làm Việc

### 4.1 Flow đọc code hiệu quả

Khi có yêu cầu mới, nên đọc theo thứ tự:

1. Controller liên quan
2. DTO request/response liên quan
3. Facade
4. Service implementation
5. Entity / repository nếu cần hiểu sâu nghiệp vụ hoặc dữ liệu

### 4.2 Đặc điểm codebase

- Controller thường mỏng, chuyển sang facade
- Permission thường đặt ngay ở controller bằng `@PreAuthorize`
- Nghiệp vụ chính nằm trong service
- Response thường bọc bằng `ApiResponse<T>`
- Danh sách phân trang thường dùng `PageResponse<T>`
- Các facade nghiệp vụ chính hiện đã chuyển sang phụ thuộc command/query thay vì gọi trực tiếp service kiểu cũ; các lớp service trung gian chỉ dùng để chuyển tiếp đã được dọn bớt để giảm chồng chéo kiến trúc

### 4.3 Khi sửa API

Nếu sửa endpoint hoặc payload:

1. Kiểm tra controller
2. Kiểm tra DTO request/response
3. Kiểm tra service xem có rule nghiệp vụ ẩn không
4. Cập nhật lại `API_DOCUMENTATION.md`
5. Cập nhật lại file `PROJECT_MEMORY.md` này nếu thay đổi có tính dài hạn

---

## 5. Security Model Quan Trọng

### 5.1 Dự án này không nên nghĩ đơn giản là USER / ADMIN

Hệ thống phân quyền theo:

- Role để nhóm quyền
- Permission để authorize API

Một role tùy biến vẫn dùng được nếu có đúng permission.

### 5.2 Các role seed đã thấy trong migration

- `SUPER_ADMIN`
- `ADMIN`
- `CONTENT_EDITOR`
- `FIELD_STAFF`
- `OPERATOR`
- `USER`

### 5.3 Một số permission quan trọng đang dùng thật ở controller

- `user.create`
- `user.view`
- `user.update`
- `user.block`
- `user.delete`
- `destination.view`
- `destination.create`
- `destination.update`
- `destination.delete`
- `destination.review`
- `destination.publish`
- `destination.propose`
- `tour.create`
- `tour.update`
- `tour.delete`
- `booking.create`
- `booking.view`
- `payment.create`
- `payment.view`
- `refund.create`
- `refund.view`
- `refund.approve`
- `refund.process`
- `review.create`
- `review.view`
- `review.reply`
- `review.moderate`

### 5.4 Backoffice roles theo source

`AuthenticatedUserProvider` đang coi các role sau là backoffice:

- `SUPER_ADMIN`
- `ADMIN`
- `OPERATOR`
- `FIELD_STAFF`
- `CONTENT_EDITOR`

Điều này ảnh hưởng đến các logic kiểu:

- user thường chỉ thao tác trên dữ liệu của chính họ
- backoffice có thể thao tác rộng hơn

---

## 6. Quy Ước Response Và Exception

### Response thành công

- `ApiResponse<T>`
- Có dạng:
  - `success`
  - `message`
  - `data`

### Response phân trang

- `PageResponse<T>`
- Có các field:
  - `content`
  - `page`
  - `size`
  - `totalElements`
  - `totalPages`
  - `last`

### Exception layer cần nhớ

Các file đáng chú ý:

- `common/exception/ErrorResponse.java`
- `common/exception/GlobalExceptionHandler.java`
- `common/exception/BadRequestException.java`
- `common/exception/ResourceNotFoundException.java`

### Security error

Các file đáng chú ý:

- `common/security/RestAuthenticationEntryPoint.java`
- `common/security/RestAccessDeniedHandler.java`

---

## 7. Những Quy Tắc Nghiệp Vụ Đã Xác Nhận

### Bookings

- `POST /bookings` cần permission `booking.create`
- `GET /bookings/{id}` cần `booking.view`
- `GET /bookings/{id}/status-history` cần `booking.view`
- `PATCH /bookings/{id}/cancel` cần `booking.cancel`
- `PATCH /bookings/{id}/check-in` cần `booking.checkin`
- `PATCH /bookings/{id}/complete` cần `booking.update`
- `userId`, `tourId`, `scheduleId`, `contactName`, `contactPhone` là bắt buộc
- `adults` tối thiểu là `1`
- Với user thường, backend ưu tiên user hiện tại từ token
- `scheduleId` phải tồn tại, phải thuộc đúng `tourId`, và schedule phải ở trạng thái `open`
- Nếu `bookingOpenAt` / `bookingCloseAt` có dữ liệu thì booking phải nằm trong cửa sổ mở bán
- `subtotalAmount` và `finalAmount` hiện đã được tính theo giá của `tour_schedule`
- Kiểm tra sức chứa schedule chỉ tính nhóm chiếm ghế: `adults + children + seniors`
- `passengers[].dateOfBirth` hiện đã được parse theo `yyyy-MM-dd` và map xuống entity
- `booking_status_history` hiện đã được ghi ở application layer khi tạo booking và khi booking đổi trạng thái
- `PATCH /bookings/{id}/cancel`:
  - `pending_payment` / `confirmed` chưa thanh toán -> `cancelled`
  - booking đã thanh toán -> `cancel_requested`
- `PATCH /bookings/{id}/check-in`: chỉ hợp lệ với booking `confirmed` và `paymentStatus = paid`
- `PATCH /bookings/{id}/complete`: chỉ hợp lệ với booking `checked_in`

### Payments

- `POST /payments` cần `payment.create`
- `GET /payments/{id}` cần `payment.view`
- Backend tự set:
  - `currency = "VND"`
  - `status = "paid"`
  - `paidAt = now()`
- `amount` phải lớn hơn `0` và phải khớp với `booking.finalAmount`
- Booking chỉ được thanh toán khi `status` là `pending_payment` hoặc `confirmed`
- Nếu payment thành công, backend cập nhật `booking.status = confirmed` và `booking.paymentStatus = paid`
- Backend chặn tạo thêm successful payment nếu booking đã `paid` / `refunded` hoặc đã có payment `paid` trước đó

### Refunds

- `POST /refunds` cần `refund.create`
- `GET /refunds/{id}` cần `refund.view`
- `PATCH /refunds/{id}/approve` cần `refund.approve` hoặc `refund.process`
- Backend gọi stored procedure `sp_get_refund_quote`
- Chỉ booking đã `paid` mới được tạo refund request
- `requestedAmount` phải lớn hơn `0`, không vượt `booking.finalAmount`, và không vượt quote trả về từ procedure
- Backend chặn tạo refund request mới nếu booking đã có refund đang active
- Approve refund sẽ:
  - đổi refund status thành `approved`
  - tạo thêm payment record hoàn tiền
  - cập nhật booking status thành `refunded`
  - cập nhật booking payment status thành `refunded`
  - chặn approve nếu `approvedAmount` vượt `requestedAmount` hoặc `quotedAmount`
  - đồng bộ lại `tour.totalBookings` và `tour_schedule.bookedSeats` ở application layer

### Reviews

- `POST /reviews` cần `review.create`
- `GET /reviews/{id}`, `/reviews/tours/{tourId}`, `/reviews/me` cần `review.view`
- `POST /reviews/{id}/replies` cần `review.reply`
- `PATCH /reviews/{id}/moderation` cần `review.moderate`
- Chỉ booking có status `checked_in` hoặc `completed` mới được review
- Mỗi booking chỉ được review một lần
- `sentiment` ban đầu mặc định là `neutral`
- Sau khi tạo review, backend đồng bộ lại `tour.averageRating` và `tour.totalReviews`

### Destinations

- `POST /destinations/propose` cần `destination.propose` hoặc `destination.create`
- Follow destination chỉ yêu cầu đăng nhập, không yêu cầu permission riêng
- Admin destination dùng các permission `destination.view/create/update/delete/review/publish`

### Tours

- `POST /admin/tours` và `PUT /admin/tours/{id}` hiện nhận thêm nested content:
  - `tagIds`
  - `media`
  - `seasonality`
  - `itineraryDays[].items`
  - `checklistItems`
- `POST /admin/tours` và `PUT /admin/tours/{id}` hiện hỗ trợ `cancellationPolicyId`
- `GET /tours/{id}` hiện trả kèm `tags`, `media`, `seasonality`, `itineraryDays[].items`, `checklistItems`
- `GET /tours/{id}` hiện trả thêm `cancellationPolicy` cùng `rules`
- `GET /tours` vẫn giữ response mỏng hơn và không load các collection lớn này
- `GET /tours` hiện chỉ trả tour `active`, filter được theo `destinationId`, `keyword`, `tagIds`, `minPrice`, `maxPrice`, `travelMonth`, `featuredOnly`, `studentFriendlyOnly`, `familyFriendlyOnly`, `seniorFriendlyOnly`
- `GET /tours` hiện filter thêm được theo `difficultyLevel`, `activityLevel`, `minDurationDays`, `maxDurationDays`
- `GET /tours` hiện filter thêm được theo `travellerAge`, `groupSize`, `tripMode`, `transportType`, `minRating`
- `GET /tours` dùng `keyword` để match `name`, `slug`, `shortDescription`, `description`, `highlights`
- Nếu `tagIds` không match tour nào, backend trả page rỗng ngay thay vì query catalog rộng
- Nếu `travelMonth` không match seasonality nào, backend trả page rỗng ngay thay vì query catalog rộng
- Các cờ `featuredOnly`, `studentFriendlyOnly`, `familyFriendlyOnly`, `seniorFriendlyOnly` chỉ áp filter khi client truyền `true`
- `difficultyLevel` và `activityLevel` cho public search phải nằm trong `1..5`
- `GET /tours` reject `maxDurationDays < minDurationDays`
- `travellerAge` cho public search được map qua `minAge/maxAge` của tour
- `groupSize` cho public search được map qua `minGroupSize/maxGroupSize` của tour
- `tripMode` cho public search chỉ nhận `group|private|shared`
- `transportType` cho public search dùng match `containsIgnoreCase`
- `minRating` cho public search phải nằm trong `0..5` và map qua `tour.averageRating`
- `GET /tours` hiện cho sort theo `name`, `basePrice`, `durationDays`, `averageRating`, `totalBookings`, `createdAt`
- `GET /tours` validate `travelMonth` trong `1..12`, `page >= 0`, `1 <= size <= 100`, `sortDir in {asc,desc}`
- `GET /tours` reject `maxPrice < minPrice`
- App layer hiện đã tự sync `tour.totalBookings` theo các booking status `confirmed|checked_in|completed`
- App layer hiện đã tự sync `tour.averageRating` và `tour.totalReviews` sau khi tạo review
- App layer hiện đã tự sync `tour_schedule.bookedSeats` theo tổng `adults + children + seniors`, và tự đổi `open <-> full` theo sức chứa
- Tour root create/update hiện replace toàn bộ child list `media`, `itineraryDays/items`, `checklistItems` theo payload thay vì patch từng phần tử
- Nếu request tour không truyền `cancellationPolicyId`, backend tự bind `default active cancellation policy`
- Nếu request truyền `cancellationPolicyId`, policy phải tồn tại, active, và phải có ít nhất một rule
- Validation nội dung tour hiện chặn:
  - `tagIds` trùng nhau hoặc chứa id không hợp lệ
  - `seasonality[].seasonName` trùng nhau
  - `seasonality[].monthTo` nhỏ hơn `monthFrom`
  - `seasonality[].recommendationScore` âm
  - `media[].sortOrder` trùng nhau trong cùng tour
  - `itineraryDays[].dayNumber` trùng nhau hoặc vượt `durationDays`
  - `itineraryDays[].items[].sequenceNo` trùng nhau trong cùng ngày
  - `itineraryDays[].items[].endTime` trước `startTime`
  - `checklistItems[].itemName` trùng nhau trong cùng tour
- `POST /admin/tours/{tourId}/schedules` cần `schedule.create`
- `PUT /admin/tours/{tourId}/schedules/{scheduleId}` cần `schedule.update`
- `PATCH /admin/tours/{tourId}/schedules/{scheduleId}/status` cần `schedule.close`
- `GET /admin/tours/{tourId}/schedules` và `GET /admin/tours/{tourId}/schedules/{scheduleId}` cần `schedule.view`
- `GET /tours/{id}/schedules` chỉ trả các schedule chưa soft-delete với status thuộc: `open`, `closed`, `full`, `departed`, `completed`
- Schedule request hiện validate:
  - `returnAt` phải sau `departureAt`
  - `bookingCloseAt` phải sau `bookingOpenAt` và không được sau `departureAt`
  - `meetingAt` không được sau `departureAt`
  - `minGuestsToOperate` không được lớn hơn `capacityTotal`
  - các giá phải `>= 0`
  - `pickupPoints[].pickupAt` không được sau `departureAt`
  - `guideAssignments[].guideId` nếu có thì phải `> 0`, không được trùng nhau, và guide phải đang `active`
- Nếu `status` của schedule bị bỏ trống thì backend mặc định `draft`
- Nếu `scheduleCode` bị bỏ trống thì backend tự sinh `SCH<timestamp>`
- Update schedule hiện replace toàn bộ child list `pickupPoints` và `guideAssignments` thay vì patch từng phần tử
- Schedule response hiện enrich `guideAssignments` bằng thông tin guide cơ bản: `guideCode`, `guideFullName`, `guidePhone`, `guideEmail`, `guideStatus`, `isLocalGuide`
- Không cho schedule đã có `bookedSeats > 0` quay lại `draft`
- Không cho reopen schedule quá ngày khởi hành về `open` hoặc `full`
- Nếu caller set `status = open` nhưng số ghế đã đầy, backend tự chuyển status thành `full`

---

## 8. Tài Liệu Đã Có Và Cách Dùng

### File tài liệu quan trọng

- `API_DOCUMENTATION.md`
- `README.md`
- `ERD.sql`
- `AGENTS.md`

### Trạng thái hiện tại của tài liệu API

- `API_DOCUMENTATION.md` đã được chỉnh lại theo source code thật
- Đã chuyển sang tiếng Việt có dấu
- Đã sửa lại phần quyền truy cập theo permission thay vì role cứng

### Trạng thái hiện tại của README

- `README.md` đã được viết lại thành hồ sơ kỹ thuật cấp dự án, không còn là bản ghi sửa lỗi cũ
- README hiện tập trung vào:
  - stack và thư viện đang dùng
  - cấu trúc module và layer
  - các quyết định kỹ thuật nhỏ nhưng quan trọng
  - cách cấu hình runtime, database, security, cache, logging, test
  - đánh giá hiện trạng codebase
- Khi framework, security model, migration, test strategy hoặc cấu trúc module thay đổi, nên cập nhật lại `README.md`

### Lưu ý

- Không tin tài liệu cũ hoặc comment cũ nếu chúng mâu thuẫn với source
- Khi có nghi ngờ, source code là nguồn đúng nhất

---

## 9. Phong Cách Làm Việc Mà Chủ Repo Mong Muốn

Đây là những điều đã thể hiện rõ qua yêu cầu của người dùng:

- Muốn tài liệu và code đồng bộ, không chấp nhận kiểu “bổ sung ở chỗ khác” gây khó đọc
- Muốn tiếng Việt có dấu trong phần mô tả tài liệu
- Muốn agent nhớ bối cảnh dự án lâu dài để tăng tốc các phiên sau
- Muốn thay đổi được triển khai tiếp nối nhanh, không phải phân tích lại toàn bộ từ đầu
- Ưu tiên cách làm thực dụng, đi thẳng vào sửa đúng chỗ thay vì trình bày dài dòng

### Khi viết code hoặc tài liệu cho repo này

- Ưu tiên ngắn gọn nhưng chính xác
- Không mô tả quyền truy cập theo role nếu thực tế code dùng permission
- Nếu sửa tài liệu, sửa ngay section gốc để đồng bộ, tránh chèn phần “bổ sung” tách rời
- Nếu sửa API hoặc nghiệp vụ dài hạn, cập nhật lại file nhớ này

---

## 10. Cách Cập Nhật File Này Sau Mỗi Lần Làm Việc

Chỉ cập nhật khi thay đổi có tính nền tảng hoặc hữu ích cho các phiên sau, ví dụ:

- thêm module mới
- đổi permission / security model
- đổi base URL / context-path / runtime flow
- phát hiện quy tắc nghiệp vụ quan trọng
- thay đổi phong cách tài liệu / quy ước làm việc của repo

Không cần cập nhật cho các thay đổi quá nhỏ như:

- đổi tên biến cục bộ
- sửa typo
- chỉnh vài dòng log không ảnh hưởng flow

### Mẫu cập nhật ngắn

Mỗi khi có thay đổi lớn, nên bổ sung:

- thay đổi gì
- ảnh hưởng ở đâu
- cần nhớ gì cho lần sau

---

## 11. Điểm Khởi Đầu Nhanh Cho Các Yêu Cầu Thường Gặp

### Nếu user hỏi về API

Đọc theo thứ tự:

1. `API_DOCUMENTATION.md`
2. Controller liên quan
3. DTO liên quan
4. Service liên quan

### Nếu user hỏi về lỗi auth / quyền

Đọc theo thứ tự:

1. Controller có `@PreAuthorize`
2. `CustomUserDetails`
3. `AuthenticatedUserProvider`
4. `SecurityConfig`
5. `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`

### Nếu user hỏi về lỗi nghiệp vụ booking / payment / refund / review

Đọc theo thứ tự:

1. Controller module
2. Service implementation
3. Entity
4. Repository
5. Exception handler nếu lỗi trả ra sai format

---

## 12. Cam Kết Sử Dụng File Này

Từ thời điểm file này được tạo:

- Mỗi phiên mới nên đọc `PROJECT_MEMORY.md` trước
- Sau đó chỉ mở thêm các file thật sự liên quan đến yêu cầu
- Khi hoàn thành thay đổi có giá trị dài hạn, cần cập nhật lại file này

Điều này sẽ giúp:

- giảm số lần phải đọc lại toàn bộ repo
- giảm token cho phần khởi động ngữ cảnh
- vẫn giữ được độ hiểu sâu về dự án nếu file được duy trì đều
---

## 13. Theo Doi Audit Phase 0

- Repo da co file `PHASE_0_AUDIT.md` de theo doi baseline trien khai theo `ERD.sql`
- File nay dung de ghi:
  - ma tran coverage `table -> code layer -> test`
  - cac bang da co schema nhung chua co feature backend
  - cac status/enum con dang dung `String` va can chuan hoa truoc khi vao Phase 1
- Khi tiep tuc roadmap nhieu dot, nen doc `PHASE_0_AUDIT.md` cung voi `PROJECT_MEMORY.md`
- Repo da co them `PHASE_0_IMPLEMENTATION_CHECKLIST.md` de theo doi tung bang theo layer va nhan `implemented` / `mvp` / `schema-only`
- Da chuan hoa cac lifecycle status cho `booking`, `payment`, `refund`, `tour`, `tour_schedule` bang enum/converter trong code thay vi `String` literal
- Da bo sung test baseline cap service cho `tours`, `bookings`, `payments`, `reviews`
- `mvnw.cmd` da duoc sua de chay duoc tren moi truong Windows hien tai; baseline tests da chay qua bang Maven wrapper
- Khi chay test voi Java 25 hien co warning lien quan Lombok `Unsafe` va Mockito dynamic agent; chua block build nhung nen xu ly sau
- `AGENTS.md` da duoc bo sung quy uoc doc `PHASE_0_AUDIT.md` va `PHASE_0_IMPLEMENTATION_CHECKLIST.md` khi lam viec theo roadmap/ERD, dong thoi nhac dong bo `API_DOCUMENTATION.md` va uu tien `mvnw.cmd` tren Windows
- `README.md` da duoc cap nhat de phan anh ket qua Phase 0, bo test baseline moi, tai lieu audit/checklist, va huong dan chay Maven wrapper tren Windows
- Task `2.1` dang/khoi dong trong Phase 2 voi huong giu `user_addresses` ben trong flow `users/profile`, khong tach module rieng
- `UserProfileController` co bo endpoint address book: `GET/POST/PUT /users/me/addresses`, `PATCH /users/me/addresses/{id}/default`, `DELETE /users/me/addresses/{id}`
- `UserProfileController` co them preference endpoints: `GET /users/me/preferences`, `PUT /users/me/preferences`
- `UserProfileController` co them device endpoints: `GET /users/me/devices`, `POST /users/me/devices`, `DELETE /users/me/devices/{id}`
- Da co them `AdminRbacController` cho read-only RBAC endpoints: `GET /roles`, `GET /roles/{id}`, `GET /permissions`
- `AdminRbacController` da co them write endpoints: `POST /roles`, `PUT /roles/{id}`, `PATCH /roles/{id}/permissions`
- Rule address book da chot:
  - chi user hien tai duoc quan ly address cua minh
  - address dau tien tu dong la default
  - set `isDefault = true` se clear default khac
  - khong cho unset default bang `isDefault = false` truc tiep
  - xoa default address thi address tiep theo theo `id` nho nhat se duoc promote len default
- Rule `user_preferences` da chot:
  - `GET` tra default virtual response neu user chua co record
  - `PUT` la upsert theo `user_id`
  - list JSON (`favoriteRegions`, `favoriteTags`, `favoriteDestinations`) duoc trim, bo phan tu rong va remove duplicate theo thu tu goc
  - booleans co default false/true dung theo schema neu request bo trong
- Rule `user_devices` da chot:
  - `GET` chi tra device active cua user hien tai
  - `POST` dung `pushToken` de upsert/reactivate neu record cua cung user da ton tai
  - `DELETE` la soft delete bang `isActive = false`, khong xoa cung
  - `platform` duoc normalize lowercase; can it nhat `deviceName` hoac `pushToken`
- Rule `RBAC read APIs` da chot:
  - endpoints la `/roles`, `/roles/{id}`, `/permissions`
  - tam thoi gate bang authority `user.view` vi codebase chua co permission rieng cho role/permission read
  - `RoleRepository` dung entity graph de load `permissions` cung role trong read flow
  - `RoleResponse` tra nested permission list da duoc sort on service layer theo `moduleName`, `actionName`, `code`
- Rule `RBAC write APIs` da chot:
  - endpoints la `POST /roles`, `PUT /roles/{id}`, `PATCH /roles/{id}/permissions`
  - write flow gate bang authority `role.assign`
  - role code duoc normalize uppercase; permission code duoc normalize lowercase
  - system role (`roleScope = SYSTEM` hoac `isSystemRole = true`) chi `SUPER_ADMIN` moi duoc create/modify
  - patch permissions la replace-toan-bo danh sach permission cua role, khong merge incrementally
  - permission inactive hoac khong ton tai se bi reject
- Rule `audit_logs` da chot:
  - endpoint admin la `GET /audit-logs`, gate bang authority `audit.view`
  - bo loc gom `actorUserId`, `actionName`, `entityName`, `entityId`, `from`, `to`; sort `createdAt desc`
  - `from` phai `<= to`
  - `AuditLogService` serialize `oldData/newData` thanh JSON string khi ghi, va parse lai thanh `JsonNode` khi query
  - `ipAddress` va `userAgent` duoc lay tu request scope neu co
  - `AuditTrailRecorder` la helper dung chung de centralize `actionName/entityName` va tranh scatter string literal o tung service
  - producer scope hien tai da duoc noi vao admin write flow: `role.create`, `role.update`, `permission.assign`, `user.create`, `user.update`, `user.deactivate`
- `UserProfileFacade.updateMyProfile` khong con validate unique o facade; de command/service xu ly voi `currentUserId` dung scope
