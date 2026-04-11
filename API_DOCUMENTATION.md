# API Documentation (tự động trích từ source)

Mô tả: file này liệt kê các endpoint chính của backend (controller, method, path, auth, request/response DTO) và kèm ví dụ curl.

Checklist

- [x] Quét controller chính trong `src/main/java/.../module` và trích endpoint
- [x] Ghi method, path, auth/role, request DTO, response DTO
- [x] Thêm ví dụ curl cho mỗi nhóm endpoint

---

## Auth (base: /auth)

### POST /auth/register

- Request body: `RegisterRequest`
- Response: `ApiResponse<AuthResponse>`
- Auth: public
- Mô tả: đăng ký tài khoản
- Ví dụ curl:

curl -X POST "http://localhost:8080/auth/register" -H "Content-Type: application/json" -d '{"email":"user@example.com","password":"Passw0rd"}'

### POST /auth/login

- Request body: `LoginRequest`
- Response: `ApiResponse<AuthResponse>`
- Auth: public
- Mô tả: đăng nhập, trả token
- Ví dụ curl:

curl -X POST "http://localhost:8080/auth/login" -H "Content-Type: application/json" -d '{"email":"user@example.com","password":"Passw0rd"}'

---

## Users (admin) (base: /users) — requires ROLE_ADMIN

### POST /users

- Body: `AdminCreateUserRequest`
- Response: `ApiResponse<UserResponse>`
- Mô tả: tạo user (ADMIN)

### GET /users

- Query / ModelAttribute: `UserSearchRequest` (paging, filters)
- Response: `ApiResponse<PageResponse<UserResponse>>`
- Mô tả: lấy danh sách/search user

### GET /users/{id}

- Path: `id` (UUID)
- Response: `ApiResponse<UserResponse>`

### PUT /users/{id}

- Body: `AdminUpdateUserRequest`
- Response: `ApiResponse<UserResponse>`

### PATCH /users/{id}/deactivate

- Response: `ApiResponse<UserResponse>`
- Mô tả: deactivate user

Ví dụ curl (lấy list, thay TOKEN bằng JWT của admin):

curl -X GET "http://localhost:8080/users?page=0&size=10" -H "Authorization: Bearer TOKEN"

---

## Current user profile (base: /users/me) — requires authentication

### GET /users/me

- Response: `ApiResponse<UserResponse>`
- Mô tả: lấy profile user đang đăng nhập

### PUT /users/me

- Body: `UpdateMyProfileRequest`
- Response: `ApiResponse<UserResponse>`
- Mô tả: cập nhật profile của chính mình

Ví dụ curl:

curl -X GET "http://localhost:8080/users/me" -H "Authorization: Bearer TOKEN"

---

## Destinations (base: /destinations)

### GET /destinations

- Query: `DestinationSearchRequest` (paging, filters)
- Response: `ApiResponse<PageResponse<DestinationResponse>>`
- Mô tả: tìm kiếm destinations đã được approve

### GET /destinations/{uuid}

- Path: `uuid` (UUID)
- Response: `ApiResponse<DestinationDetailResponse>`

### POST /destinations/propose

- Auth: `isAuthenticated()` (cần đăng nhập)
- Body: `DestinationRequest`
- Response: `ApiResponse<DestinationDetailResponse>` (201 Created)
- Mô tả: user propose destination, chờ admin review

Ví dụ curl (propose):

curl -X POST "http://localhost:8080/destinations/propose" -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d '{"name":"Example","description":"..."}'

---

## Destination follow (base: /destinations) — requires authentication

### POST /destinations/{uuid}/follow

- Body (optional): `FollowDestinationRequest`
- Response: `ApiResponse<DestinationFollowResponse>` (201 Created)
- Mô tả: follow destination

### DELETE /destinations/{uuid}/follow

- Response: `ApiResponse<Void>`
- Mô tả: unfollow

### PUT /destinations/{uuid}/follow/settings

- Body: `FollowDestinationRequest`
- Response: `ApiResponse<DestinationFollowResponse>`

### GET /destinations/me/follows?page={page}&size={size}

- Response: `ApiResponse<PageResponse<DestinationFollowResponse>>`
- Mô tả: lấy follows của user hiện tại

Ví dụ curl (follow):

curl -X POST "http://localhost:8080/destinations/{uuid}/follow" -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" -d '{"notify":true}'

---

## Admin destinations (base: /admin/destinations) — requires ROLE_ADMIN

### GET /admin/destinations

- Query: `DestinationSearchRequest`
- Response: `ApiResponse<PageResponse<DestinationResponse>>`

### GET /admin/destinations/{uuid}

- Response: `ApiResponse<DestinationDetailResponse>`

### POST /admin/destinations

- Body: `DestinationRequest`
- Response: `ApiResponse<DestinationDetailResponse>` (201 Created)

### PUT /admin/destinations/{uuid}

- Body: `DestinationRequest`
- Response: `ApiResponse<DestinationDetailResponse>`

### DELETE /admin/destinations/{uuid}

- Response: `ApiResponse<Void>` (204 No Content)

### PATCH /admin/destinations/{uuid}/approve

- Response: `ApiResponse<DestinationDetailResponse>`

### PATCH /admin/destinations/{uuid}/reject

- Body: `RejectProposalRequest`
- Response: `ApiResponse<DestinationDetailResponse>`

Ví dụ curl (admin approve):

curl -X PATCH "http://localhost:8080/admin/destinations/{uuid}/approve" -H "Authorization: Bearer ADMIN_TOKEN"

---

## System

### GET /system/health

- Response: `ApiResponse<Map<String,Object>>` (service, status, time)
- Mô tả: health check

---

## Tours (base: /tours)

### GET /tours

- Query: `TourSearchRequest` (paging, filters)
- Response: `ApiResponse<PageResponse<TourResponse>>`
- Mô tả: tìm kiếm tours

### GET /tours/{id}

- Path: `id` (Long)
- Response: `ApiResponse<TourResponse>`

---

## Admin tours (base: /admin/tours) — requires ROLE_ADMIN

### POST /admin/tours

- Body: `TourRequest`
- Response: `ApiResponse<TourResponse>` (201 Created)

### PUT /admin/tours/{id}

- Body: `TourRequest`
- Response: `ApiResponse<TourResponse>`

### DELETE /admin/tours/{id}

- Response: `ApiResponse<String>`

---

## Bookings (base: /bookings)

### POST /bookings

- Body: `CreateBookingRequest`
- Response: `ApiResponse<BookingResponse>` (201 Created)

### GET /bookings/{id}

- Path: `id` (Long)
- Response: `ApiResponse<BookingResponse>`

---

## Payments (base: /payments)

### POST /payments

- Body: `CreatePaymentRequest`
- Response: `ApiResponse<PaymentResponse>` (201 Created)

### GET /payments/{id}

- Path: `id` (Long)
- Response: `ApiResponse<PaymentResponse>`

---

## Refunds (base: /refunds)

### POST /refunds

- Body: `CreateRefundRequest`
- Response: `ApiResponse<RefundResponse>` (201 Created)

### GET /refunds/{id}

- Path: `id` (Long)
- Response: `ApiResponse<RefundResponse>`

### PATCH /refunds/{id}/approve

- Body: `ApproveRefundRequest` (approvedAmount)
- Response: `ApiResponse<RefundResponse>`

---

## Ghi chú & bước tiếp theo

- Các DTO/responses được tham chiếu theo tên lớp (xem `src/main/java/.../dto`) nếu cần mẫu JSON chi tiết tôi có thể thêm.
- Muốn export sang CSV/JSON hoặc thêm ví dụ request/response chi tiết cho từng DTO không? Gõ `csv` hoặc `example` để tôi tiếp tục.
