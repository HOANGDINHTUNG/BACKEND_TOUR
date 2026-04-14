# Tài Liệu API - TravelViet Booking System

> Base URL: `http://localhost:8088/api/v1`
> Content-Type: `application/json`
> Authentication: `Authorization: Bearer <token>`

---

## 1. Mô Hình Phân Quyền

### 1.1 Hệ thống hiện tại đang phân quyền theo permission

Ở source hiện tại, việc phân quyền API không nên đọc theo kiểu "chỉ USER / ADMIN" như tài liệu cũ.
Hệ thống đang dùng:

- Role để nhóm quyền
- Permission để kiểm tra truy cập API qua `@PreAuthorize`
- Một user có thể có nhiều role
- Một role tùy biến vẫn dùng được nếu role đó có đúng permission

### 1.2 Các role seed hiện có trong migration

| Role code | Scope | Mô tả |
| --- | --- | --- |
| `SUPER_ADMIN` | SYSTEM | Toàn quyền hệ thống |
| `ADMIN` | BACKOFFICE | Quản trị vận hành |
| `CONTENT_EDITOR` | BACKOFFICE | Quản lý nội dung destination, tour, media |
| `FIELD_STAFF` | BACKOFFICE | Nhân sự thực địa, cập nhật dữ liệu, check-in |
| `OPERATOR` | BACKOFFICE | Điều phối schedule, booking, refund, support |
| `USER` | CUSTOMER | Khách hàng sử dụng ứng dụng |

### 1.3 Lưu ý cho role tùy biến

- Tài liệu này ưu tiên ghi theo `permission` của API.
- Nếu bạn tạo thêm role mới, role đó vẫn gọi được API nếu được gán đúng permission.
- Vì vậy không nên đọc tài liệu theo kiểu "API này chỉ ADMIN", mà nên đọc theo cột `Permission`.

### 1.4 Quy ước trong tài liệu này

| Mục | Nghĩa |
| --- | --- |
| `PUBLIC` | Không cần token |
| `AUTHENTICATED` | Chỉ cần đăng nhập |
| `Permission: xxx.yyy` | Cần đúng authority đó |

---

## 2. Dữ Liệu Test Dùng Chung

### 2.1 Header mẫu

```http
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

### 2.2 Biến test gợi ý

```text
ACCESS_TOKEN=<jwt_token>
REFRESH_TOKEN=<refresh_token>
USER_ID=550e8400-e29b-41d4-a716-446655440000
DESTINATION_UUID=3fa85f64-5717-4562-b3fc-2c963f66afa6
TOUR_ID=1
SCHEDULE_ID=5
BOOKING_ID=1
PAYMENT_ID=1
REFUND_ID=1
REVIEW_ID=1
```

### 2.3 User test để dùng lại

```json
{
  "fullName": "Nguyễn Văn An",
  "email": "an.nguyen+api@gmail.com",
  "phone": "+84901234567",
  "passwordHash": "Password@123",
  "displayName": "An Nguyen",
  "gender": "male",
  "dateOfBirth": "1995-06-15"
}
```

### 2.4 Response wrapper chung

Mọi API đều trả theo `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

Nếu là phân trang, `data` thường là `PageResponse<T>`:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

---

## 3. System

### `GET /system/health`

- Access: `PUBLIC`
- Mô tả: Health check backend

**Request**

```http
GET http://localhost:8088/api/v1/system/health
```

**Response**

```json
{
  "success": true,
  "message": "Application is running",
  "data": {
    "service": "wedservice-backend",
    "status": "OK",
    "time": "2026-04-14T23:00:00"
  }
}
```

---

## 4. Auth

### `POST /auth/register`

- Access: `PUBLIC`
- Mô tả: Đăng ký tài khoản mới

**Rules**

- `fullName`: bắt buộc, max 150
- `email` hoặc `phone`: phải có ít nhất một
- `email`: đúng định dạng email
- `phone`: regex `^[+]?[0-9]{8,20}$`
- `passwordHash`: bắt buộc, 8-255
- `dateOfBirth`: phải là ngày trong quá khứ

**Request**

```http
POST http://localhost:8088/api/v1/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Nguyễn Văn An",
  "email": "an.nguyen+api@gmail.com",
  "phone": "+84901234567",
  "passwordHash": "Password@123",
  "displayName": "An Nguyen",
  "gender": "male",
  "dateOfBirth": "1995-06-15",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

### `POST /auth/login`

- Access: `PUBLIC`
- Mô tả: Đăng nhập bằng email hoặc phone

**Request**

```json
{
  "login": "an.nguyen+api@gmail.com",
  "passwordHash": "Password@123"
}
```

`login` cũng nhận alias `email`.

### `POST /auth/refresh`

- Access: `PUBLIC`
- Mô tả: Lấy cặp token mới bằng refresh token

**Request**

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

### Auth response shape

`register`, `login`, `refresh` đều trả `AuthResponse`:

```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "an.nguyen+api@gmail.com",
      "phone": "+84901234567",
      "fullName": "Nguyễn Văn An",
      "displayName": "An Nguyen",
      "gender": "male",
      "dateOfBirth": "1995-06-15",
      "avatarUrl": "https://example.com/avatar.jpg",
      "userCategory": "CUSTOMER",
      "role": "USER",
      "roles": ["USER"],
      "status": "active",
      "memberLevel": "bronze",
      "loyaltyPoints": 0,
      "totalSpent": 0
    },
    "tokenType": "Bearer",
    "accessToken": "<ACCESS_TOKEN>",
    "expiresIn": 3600000,
    "refreshToken": "<REFRESH_TOKEN>",
    "refreshExpiresIn": 2592000000
  }
}
```

---

## 5. Users

### 5.1 User Profile

#### `GET /users/me`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`

```http
GET http://localhost:8088/api/v1/users/me
Authorization: Bearer <ACCESS_TOKEN>
```

#### `PUT /users/me`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`

**Request**

```json
{
  "fullName": "Nguyễn Văn An Updated",
  "email": "an.nguyen+updated@gmail.com",
  "phone": "+84901234567",
  "displayName": "An Updated",
  "gender": "male",
  "dateOfBirth": "1995-06-15",
  "avatarUrl": "https://example.com/avatar-new.jpg"
}
```

**Rules**

- `fullName`: bắt buộc, max 150
- `email` hoặc `phone`: phải có ít nhất một
- `gender`: `male`, `female`, `other`, `unknown`

### 5.2 Admin Users

> Lưu ý: ở code hiện tại, phần này kiểm theo permission, không phải một role cố định.

#### `POST /users`

- Permission: `user.create`
- Mô tả: Tạo user mới

**Request**

```json
{
  "fullName": "Trần Thị Bình",
  "email": "binh.tran@company.com",
  "phone": "+84912345678",
  "passwordHash": "StaffPass@456",
  "userCategory": "INTERNAL",
  "roleCodes": ["OPERATOR"],
  "status": "active",
  "displayName": "Binh Operator",
  "gender": "female",
  "dateOfBirth": "1992-03-20",
  "avatarUrl": "https://example.com/binh.jpg",
  "memberLevel": "silver",
  "loyaltyPoints": 500,
  "totalSpent": 5000000,
  "emailVerifiedAt": "2026-04-14T08:00:00",
  "phoneVerifiedAt": "2026-04-14T08:00:00"
}
```

**Điểm cần lưu ý**

- Body dùng `roleCodes`, không dùng `role`
- `userCategory` bắt buộc
- `status` của code hiện tại: `pending`, `active`, `suspended`, `blocked`, `deleted`

#### `GET /users`

- Permission: `user.view`
- Mô tả: Danh sách user có phân trang và filter

**Query params thực tế**

| Param | Type | Default | Ghi chú |
| --- | --- | --- | --- |
| `page` | int | `0` | >= 0 |
| `size` | int | `10` | 1..100 |
| `keyword` | string | - | max 100 |
| `status` | enum | - | `pending`, `active`, `suspended`, `blocked`, `deleted` |
| `roleCode` | string | - | role code cần lọc |
| `memberLevel` | enum | - | `bronze`, `silver`, `gold`, `platinum`, `diamond` |
| `sortBy` | string | `createdAt` | `id`, `fullName`, `displayName`, `email`, `phone`, `userCategory`, `status`, `memberLevel`, `createdAt`, `updatedAt`, `lastLoginAt`, `deletedAt` |
| `sortDir` | string | `desc` | `asc` hoặc `desc` |

**Request**

```http
GET http://localhost:8088/api/v1/users?page=0&size=10&keyword=nguyen&status=active&roleCode=USER&sortBy=createdAt&sortDir=desc
Authorization: Bearer <ACCESS_TOKEN>
```

#### `GET /users/{id}`

- Permission: `user.view`

#### `PUT /users/{id}`

- Permission: `user.update`

**Request**

```json
{
  "fullName": "Trần Thị Bình Updated",
  "email": "binh.updated@company.com",
  "phone": "+84912345678",
  "passwordHash": "NewPass@789",
  "userCategory": "INTERNAL",
  "roleCodes": ["CONTENT_EDITOR", "OPERATOR"],
  "status": "active",
  "displayName": "Binh Updated",
  "gender": "female",
  "dateOfBirth": "1992-03-20",
  "avatarUrl": "https://example.com/binh-new.jpg",
  "memberLevel": "gold",
  "loyaltyPoints": 1500,
  "totalSpent": 15000000,
  "emailVerifiedAt": "2026-04-14T08:00:00",
  "phoneVerifiedAt": "2026-04-14T08:00:00",
  "lastLoginAt": "2026-04-14T21:30:00",
  "deletedAt": null
}
```

#### `PATCH /users/{id}/deactivate`

- Permission: `user.block` hoặc `user.delete`
- Mô tả: Vô hiệu hóa user

```http
PATCH http://localhost:8088/api/v1/users/550e8400-e29b-41d4-a716-446655440000/deactivate
Authorization: Bearer <ACCESS_TOKEN>
```

### 5.3 UserResponse shape

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "an.nguyen+api@gmail.com",
  "phone": "+84901234567",
  "fullName": "Nguyễn Văn An",
  "displayName": "An Nguyen",
  "gender": "male",
  "dateOfBirth": "1995-06-15",
  "avatarUrl": "https://example.com/avatar.jpg",
  "userCategory": "CUSTOMER",
  "role": "USER",
  "roles": ["USER"],
  "status": "active",
  "memberLevel": "bronze",
  "loyaltyPoints": 0,
  "totalSpent": 0,
  "emailVerifiedAt": null,
  "phoneVerifiedAt": null,
  "lastLoginAt": null,
  "createdAt": "2026-04-14T22:00:00",
  "updatedAt": "2026-04-14T22:00:00",
  "deletedAt": null
}
```

---

## 6. Destinations

### 6.1 Public Destinations

#### `GET /destinations`

- Access: `PUBLIC`
- Mô tả: Search approved destinations

**Query params**

| Param | Type | Default |
| --- | --- | --- |
| `keyword` | string | - |
| `province` | string | - |
| `region` | string | - |
| `crowdLevel` | enum | - |
| `isFeatured` | boolean | - |
| `page` | int | `0` |
| `size` | int | `10` |
| `sortBy` | string | `name` |
| `sortDir` | string | `asc` |

**Request**

```http
GET http://localhost:8088/api/v1/destinations?keyword=Ha+Long&province=Quang+Ninh&page=0&size=10
```

#### `GET /destinations/{uuid}`

- Access: `PUBLIC`

#### `POST /destinations/propose`

- Permission: `destination.propose` hoặc `destination.create`
- Mô tả: User đề xuất destination

**Request**

```json
{
  "name": "Vịnh Hạ Long",
  "province": "Quảng Ninh",
  "district": "Hạ Long",
  "region": "Đông Bắc",
  "countryCode": "VN",
  "address": "Hạ Long, Quảng Ninh, Việt Nam",
  "latitude": 20.9101,
  "longitude": 107.1839,
  "shortDescription": "Di sản thiên nhiên thế giới UNESCO",
  "description": "Điểm đến nổi tiếng với hàng ngàn đảo đá vôi",
  "bestTimeFromMonth": 3,
  "bestTimeToMonth": 5,
  "crowdLevelDefault": "MEDIUM"
}
```

### 6.2 Admin Destinations

#### `GET /admin/destinations`

- Permission: `destination.view`

**Query params thực tế**

Thêm các filter sau ngoài bộ public:

- `isActive`
- `isOfficial`
- `status`: `pending`, `approved`, `rejected`

#### `GET /admin/destinations/{uuid}`

- Permission: `destination.view`

#### `POST /admin/destinations`

- Permission: `destination.create`

**Request sample**

```json
{
  "code": "HA-LONG-BAY",
  "name": "Vịnh Hạ Long",
  "slug": "vinh-ha-long",
  "countryCode": "VN",
  "province": "Quảng Ninh",
  "district": "Hạ Long",
  "region": "Đông Bắc",
  "address": "Hạ Long, Quảng Ninh, Việt Nam",
  "latitude": 20.9101,
  "longitude": 107.1839,
  "shortDescription": "Di sản thiên nhiên thế giới UNESCO",
  "description": "Vịnh Hạ Long là điểm đến du lịch nổi tiếng của Việt Nam",
  "bestTimeFromMonth": 3,
  "bestTimeToMonth": 5,
  "crowdLevelDefault": "MEDIUM",
  "isFeatured": true,
  "isActive": true,
  "isOfficial": true,
  "mediaList": [
    {
      "mediaType": "IMAGE",
      "mediaUrl": "https://example.com/halong-1.jpg",
      "altText": "Ha Long overview",
      "sortOrder": 1,
      "isActive": true
    }
  ],
  "foods": [
    {
      "foodName": "Chả mực Hạ Long",
      "description": "Món đặc sản nổi tiếng",
      "isFeatured": true
    }
  ],
  "specialties": [
    {
      "specialtyName": "Sá sùng khô",
      "description": "Đặc sản biển"
    }
  ],
  "activities": [
    {
      "activityName": "Kayak",
      "description": "Chèo kayak quanh các đảo",
      "activityScore": 4.8
    }
  ],
  "tips": [
    {
      "tipTitle": "Trang phục",
      "tipContent": "Mang giày dễ đi bộ",
      "sortOrder": 1
    }
  ],
  "events": [
    {
      "eventName": "Carnaval Hạ Long",
      "eventType": "FESTIVAL",
      "description": "Sự kiện du lịch lớn",
      "startsAt": "2026-04-30T19:00:00",
      "endsAt": "2026-05-01T22:00:00",
      "notifyAllFollowers": true,
      "isActive": true
    }
  ]
}
```

#### `PUT /admin/destinations/{uuid}`

- Permission: `destination.update`
- Body: giống `POST /admin/destinations`

#### `DELETE /admin/destinations/{uuid}`

- Permission: `destination.delete`

#### `PATCH /admin/destinations/{uuid}/approve`

- Permission: `destination.review` hoặc `destination.publish`

#### `PATCH /admin/destinations/{uuid}/reject`

- Permission: `destination.review` hoặc `destination.publish`

**Request**

```json
{
  "reason": "Thông tin chưa đủ để xác minh"
}
```

### 6.3 Destination Follow

> Tất cả API phần này chỉ cần đăng nhập, không check permission riêng.

#### `POST /destinations/{uuid}/follow`

- Access: `AUTHENTICATED`
- Body có thể bỏ trống

```json
{
  "notifyEvent": true,
  "notifyVoucher": true,
  "notifyNewTour": true,
  "notifyBestSeason": false
}
```

#### `DELETE /destinations/{uuid}/follow`

- Access: `AUTHENTICATED`

#### `PUT /destinations/{uuid}/follow/settings`

- Access: `AUTHENTICATED`

```json
{
  "notifyEvent": false,
  "notifyVoucher": true,
  "notifyNewTour": false,
  "notifyBestSeason": true
}
```

#### `GET /destinations/me/follows`

- Access: `AUTHENTICATED`
- Query: `page`, `size`

---

## 7. Tours

### `GET /tours`

- Access: `PUBLIC`

**Query params**

| Param | Type | Default |
| --- | --- | --- |
| `destinationId` | long | - |
| `keyword` | string | - |
| `page` | int | `0` |
| `size` | int | `10` |

**Request**

```http
GET http://localhost:8088/api/v1/tours?keyword=Da+Nang&destinationId=1&page=0&size=10
```

### `GET /tours/{id}`

- Access: `PUBLIC`

### `POST /admin/tours`

- Permission: `tour.create`

**Request**

```json
{
  "code": "TOUR-BNH-2026",
  "name": "Tour Bà Nà Hills 2 ngày 1 đêm",
  "slug": "tour-ba-na-hills-2n1d",
  "destinationId": 1,
  "basePrice": 1500000,
  "currency": "VND",
  "durationDays": 2,
  "durationNights": 1,
  "transportType": "BUS",
  "tripMode": "GROUP",
  "highlights": "Cầu Vàng, Làng Pháp, Fantasy Park",
  "inclusions": "Xe đưa đón, cáp treo, vé vào cửa",
  "exclusions": "Chi phí cá nhân",
  "notes": "Mang giày thể thao",
  "isFeatured": true,
  "status": "ACTIVE"
}
```

### `PUT /admin/tours/{id}`

- Permission: `tour.update`
- Body: giống `POST /admin/tours`

### `DELETE /admin/tours/{id}`

- Permission: `tour.delete`

### TourResponse shape

```json
{
  "id": 1,
  "code": "TOUR-BNH-2026",
  "name": "Tour Bà Nà Hills 2 ngày 1 đêm",
  "slug": "tour-ba-na-hills-2n1d",
  "destinationId": 1,
  "basePrice": 1500000,
  "currency": "VND"
}
```

---

## 8. Bookings

### `POST /bookings`

- Permission: `booking.create`

**Lưu ý nghiệp vụ từ code**

- `userId`, `tourId`, `scheduleId`, `contactName`, `contactPhone` là bắt buộc
- `adults` tối thiểu `1`
- Nếu user thường gọi API, backend ưu tiên user trong token
- `passengers[].dateOfBirth` hiện đang không được map xuống entity khi create booking

**Request**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "tourId": 1,
  "scheduleId": 5,
  "contactName": "Nguyễn Văn An",
  "contactPhone": "+84901234567",
  "contactEmail": "an.nguyen+api@gmail.com",
  "adults": 2,
  "children": 1,
  "infants": 0,
  "seniors": 0,
  "passengers": [
    {
      "fullName": "Nguyễn Văn An",
      "passengerType": "adult",
      "gender": "male",
      "dateOfBirth": "1995-06-15",
      "identityNo": "001095012345",
      "phone": "+84901234567",
      "email": "an.nguyen+api@gmail.com"
    },
    {
      "fullName": "Trần Thị Bình",
      "passengerType": "adult",
      "gender": "female",
      "dateOfBirth": "1997-09-20",
      "identityNo": "001097067890",
      "phone": "+84912345678"
    }
  ]
}
```

**Response**

```json
{
  "success": true,
  "message": "Booking created",
  "data": {
    "id": 1,
    "bookingCode": "BK1713115800000",
    "status": "pending_payment",
    "finalAmount": 0
  }
}
```

### `GET /bookings/{id}`

- Permission: `booking.view`

---

## 9. Payments

### `POST /payments`

- Permission: `payment.create`

**Lưu ý nghiệp vụ từ code**

- DTO bắt buộc: `bookingId`, `paymentMethod`, `amount`
- Backend tự set:
  - `currency = "VND"`
  - `status = "paid"`
  - `paidAt = now()`

**Request**

```json
{
  "bookingId": 1,
  "paymentMethod": "VNPAY",
  "provider": "VNPay",
  "transactionRef": "VNPAY-TXN-20260414-001",
  "amount": 3000000
}
```

**Response**

```json
{
  "success": true,
  "message": "Payment created",
  "data": {
    "id": 1,
    "paymentCode": "PM1713115900000",
    "bookingId": 1,
    "amount": 3000000,
    "status": "paid"
  }
}
```

### `GET /payments/{id}`

- Permission: `payment.view`

---

## 10. Refunds

### `POST /refunds`

- Permission: `refund.create`

**Lưu ý nghiệp vụ từ code**

- DTO bắt buộc: `bookingId`, `requestedAmount`
- `requestedBy` nếu không phải backoffice sẽ bị override bằng user đang login
- Backend gọi stored procedure `sp_get_refund_quote`
- Status khởi tạo: `requested`

**Request**

```json
{
  "bookingId": 1,
  "requestedBy": "550e8400-e29b-41d4-a716-446655440000",
  "reasonType": "CANCEL_BY_USER",
  "reasonDetail": "Không thể tham gia tour do thay đổi lịch cá nhân",
  "requestedAmount": 2700000
}
```

**Response**

```json
{
  "success": true,
  "message": "Refund request created",
  "data": {
    "id": 1,
    "refundCode": "RF1713116000000",
    "bookingId": 1,
    "status": "requested",
    "requestedAmount": 2700000
  }
}
```

### `GET /refunds/{id}`

- Permission: `refund.view`

### `PATCH /refunds/{id}/approve`

- Permission: `refund.approve` hoặc `refund.process`

**Request**

```json
{
  "approvedAmount": 2500000
}
```

**Lưu ý nghiệp vụ từ code**

- Refund status -> `approved`
- Hệ thống tạo thêm payment record:
  - `paymentMethod = "refund"`
  - `status = "refunded"`
- Booking payment status được update thành `refunded`

---

## 11. Reviews

### `POST /reviews`

- Permission: `review.create`

**Rules từ code**

- `bookingId` bắt buộc
- `overallRating` phải trong khoảng `1..5`
- Mỗi booking chỉ được review một lần
- Chỉ booking có status `checked_in` hoặc `completed` mới được review
- `wouldRecommend` mặc định `true`
- `sentiment` ban đầu là `neutral`

**Request**

```json
{
  "bookingId": 1,
  "overallRating": 5,
  "title": "Tour rất tốt",
  "content": "Hướng dẫn viên nhiệt tình, lịch trình đúng giờ, đồ ăn ổn.",
  "wouldRecommend": true,
  "aspects": [
    {
      "aspectName": "guide",
      "aspectRating": 5,
      "comment": "Hướng dẫn viên hỗ trợ rất tốt"
    },
    {
      "aspectName": "schedule",
      "aspectRating": 4,
      "comment": "Lịch trình hợp lý"
    }
  ]
}
```

**Response**

```json
{
  "success": true,
  "message": "Review created successfully",
  "data": {
    "id": 1,
    "bookingId": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "tourId": 1,
    "scheduleId": 5,
    "overallRating": 5,
    "title": "Tour rất tốt",
    "content": "Hướng dẫn viên nhiệt tình, lịch trình đúng giờ, đồ ăn ổn.",
    "sentiment": "neutral",
    "wouldRecommend": true,
    "createdAt": "2026-04-14T23:45:00",
    "updatedAt": "2026-04-14T23:45:00",
    "aspects": [
      {
        "id": 1,
        "aspectName": "guide",
        "aspectRating": 5,
        "comment": "Hướng dẫn viên hỗ trợ rất tốt"
      }
    ],
    "replies": []
  }
}
```

### `GET /reviews/{id}`

- Permission: `review.view`

### `GET /reviews/tours/{tourId}`

- Permission: `review.view`
- Query params: `page`, `size`

### `GET /reviews/me`

- Permission: `review.view`
- Query params: `page`, `size`

### `POST /reviews/{id}/replies`

- Permission: `review.reply`

**Request**

```json
{
  "content": "Cảm ơn bạn đã đánh giá. Chúng tôi sẽ tiếp tục cải thiện chất lượng dịch vụ."
}
```

### `PATCH /reviews/{id}/moderation`

- Permission: `review.moderate`

**Request**

```json
{
  "sentiment": "positive"
}
```

**Sentiment hợp lệ**

- `positive`
- `neutral`
- `negative`
- `mixed`

---

## 12. Bảng Quyền Truy Cập Nhanh Theo API

| Endpoint | Access |
| --- | --- |
| `GET /system/health` | `PUBLIC` |
| `POST /auth/register` | `PUBLIC` |
| `POST /auth/login` | `PUBLIC` |
| `POST /auth/refresh` | `PUBLIC` |
| `GET /users/me` | `AUTHENTICATED` |
| `PUT /users/me` | `AUTHENTICATED` |
| `POST /users` | `user.create` |
| `GET /users` | `user.view` |
| `GET /users/{id}` | `user.view` |
| `PUT /users/{id}` | `user.update` |
| `PATCH /users/{id}/deactivate` | `user.block` or `user.delete` |
| `GET /destinations` | `PUBLIC` |
| `GET /destinations/{uuid}` | `PUBLIC` |
| `POST /destinations/propose` | `destination.propose` or `destination.create` |
| `GET /admin/destinations` | `destination.view` |
| `GET /admin/destinations/{uuid}` | `destination.view` |
| `POST /admin/destinations` | `destination.create` |
| `PUT /admin/destinations/{uuid}` | `destination.update` |
| `DELETE /admin/destinations/{uuid}` | `destination.delete` |
| `PATCH /admin/destinations/{uuid}/approve` | `destination.review` or `destination.publish` |
| `PATCH /admin/destinations/{uuid}/reject` | `destination.review` or `destination.publish` |
| `POST /destinations/{uuid}/follow` | `AUTHENTICATED` |
| `DELETE /destinations/{uuid}/follow` | `AUTHENTICATED` |
| `PUT /destinations/{uuid}/follow/settings` | `AUTHENTICATED` |
| `GET /destinations/me/follows` | `AUTHENTICATED` |
| `GET /tours` | `PUBLIC` |
| `GET /tours/{id}` | `PUBLIC` |
| `POST /admin/tours` | `tour.create` |
| `PUT /admin/tours/{id}` | `tour.update` |
| `DELETE /admin/tours/{id}` | `tour.delete` |
| `POST /bookings` | `booking.create` |
| `GET /bookings/{id}` | `booking.view` |
| `POST /payments` | `payment.create` |
| `GET /payments/{id}` | `payment.view` |
| `POST /refunds` | `refund.create` |
| `GET /refunds/{id}` | `refund.view` |
| `PATCH /refunds/{id}/approve` | `refund.approve` or `refund.process` |
| `POST /reviews` | `review.create` |
| `GET /reviews/{id}` | `review.view` |
| `GET /reviews/tours/{tourId}` | `review.view` |
| `GET /reviews/me` | `review.view` |
| `POST /reviews/{id}/replies` | `review.reply` |
| `PATCH /reviews/{id}/moderation` | `review.moderate` |

---

## 13. Bảng Enum Tham Khảo

### User

- `Gender`: `male`, `female`, `other`, `unknown`
- `Status`: `pending`, `active`, `suspended`, `blocked`, `deleted`
- `MemberLevel`: `bronze`, `silver`, `gold`, `platinum`, `diamond`
- `UserCategory`: `INTERNAL`, `CUSTOMER`

### Destinations

- `CrowdLevel`: `LOW`, `MEDIUM`, `HIGH`, `VERY_HIGH`
- `DestinationStatus`: `pending`, `approved`, `rejected`

### Reviews

- `sentiment`: `positive`, `neutral`, `negative`, `mixed`

### Booking / Payment / Refund

- `passengerType`: `adult`, `child`, `infant`, `senior`
- `paymentMethod`: ví dụ `CASH`, `BANK_TRANSFER`, `VNPAY`, `MOMO`, `ZALOPAY`
- `reasonType`: ví dụ `CANCEL_BY_USER`, `FORCE_CANCEL`, `DUPLICATE_BOOKING`

---

## 14. Flow Test Đề Xuất

1. `POST /auth/register`
2. `POST /auth/login`
3. `GET /users/me`
4. `POST /bookings`
5. `POST /payments`
6. `POST /refunds`
7. `POST /reviews`
8. `GET /reviews/me`
9. `POST /destinations/{uuid}/follow`
