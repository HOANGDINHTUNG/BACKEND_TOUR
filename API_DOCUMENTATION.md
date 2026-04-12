# 📘 API Documentation — TravelViet Booking System

> **Base URL:** `http://localhost:8080/api`  
> **Content-Type:** `application/json`  
> **Authentication:** Bearer JWT Token (`Authorization: Bearer <token>`)

---

## 🔐 Quy tắc phân quyền

| Role     | Mô tả                           |
| -------- | ------------------------------- |
| `PUBLIC` | Không cần đăng nhập             |
| `USER`   | Cần đăng nhập (bất kỳ role nào) |
| `ADMIN`  | Chỉ tài khoản admin             |

---

## 1. 🏥 System — Kiểm tra sức khoẻ hệ thống

### `GET /system/health`

**Tác dụng:** Kiểm tra xem backend đang chạy không (health check). Thường dùng bởi DevOps, load balancer, hoặc để test nhanh sau khi deploy.  
**Phân quyền:** PUBLIC

**Postman Sample:**

```
GET http://localhost:8080/api/system/health
```

**Response mẫu:**

```json
{
  "success": true,
  "message": "Application is running",
  "data": {
    "service": "wedservice-backend",
    "status": "OK",
    "time": "2026-04-11T21:00:00"
  }
}
```

---

## 2. 🔑 Auth — Xác thực người dùng

### `POST /auth/register`

**Tác dụng:** Đăng ký tài khoản mới. Trả về JWT token và thông tin user sau khi đăng ký thành công.  
**Phân quyền:** PUBLIC

**Điều kiện:**

- `fullName`: bắt buộc, tối đa 150 ký tự
- `email` hoặc `phone`: ít nhất một trong hai phải có
- `email` (nếu có): phải đúng định dạng email
- `phone` (nếu có): định dạng `+84xxxxxxxxx` hoặc `0xxxxxxxxx` (8–20 số)
- `passwordHash`: bắt buộc, 8–255 ký tự
- `gender`: `MALE` | `FEMALE` | `OTHER` | `PREFER_NOT_TO_SAY`
- `dateOfBirth`: phải là ngày trong quá khứ, định dạng `YYYY-MM-DD`

**Body mẫu:**

````json
{
  "fullName": "Nguyễn Văn An",
  "email": "an.nguyen@gmail.com",
  "phone": "+84901234567",
  "passwordHash": "Password@123",
  "displayName": "An Nguyen",
  "gender": "MALE",
  "dateOfBirth": "1995-06-15",
**Response mẫu:**

```json
{
  "success": true,
  "message": "Register successfully",
  "data": {
    "user": { ... },
    "tokenType": "Bearer",
    "accessToken": "eyJhbG...",
    "expiresIn": 3600000,
    "refreshToken": "eyJhbG...",
    "refreshExpiresIn": 2592000000
  }
}
````

---

### `POST /auth/login`

**Tác dụng:** Đăng nhập bằng email/SĐT và mật khẩu. Trả về JWT token để dùng cho các API cần xác thực.  
**Phân quyền:** PUBLIC

**Điều kiện:**

- `login` (hoặc `email`): bắt buộc — email hoặc số điện thoại
- `passwordHash`: bắt buộc

**Body mẫu:**

```json
{
  "login": "an.nguyen@gmail.com",
  "passwordHash": "Password@123"
}
```

> 💡 Field `login` cũng chấp nhận alias `email` nhờ `@JsonAlias`.

**Response mẫu:**

```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "user": { ... },
    "tokenType": "Bearer",
    "accessToken": "eyJhbG...",
    "expiresIn": 3600000,
    "refreshToken": "eyJhbG...",
    "refreshExpiresIn": 2592000000
  }
}
```

---

### `POST /auth/refresh`

**Tác dụng:** Dùng Refresh Token để lấy cặp Access Token và Refresh Token mới (Token Rotation). Khi Access Token hết hạn (401), app client gọi API này để duy trì đăng nhập mà không cần user nhập lại mật khẩu.  
**Phân quyền:** PUBLIC (Sử dụng Refresh Token trong body)

**Body mẫu:**

```json
{
  "refreshToken": "eyJhbGci..."
}
```

**Response mẫu:** (Giống login — trả về cặp token mới)

---

## 3. 👤 User Profile — Hồ sơ cá nhân

### `GET /users/me`

**Tác dụng:** Lấy thông tin hồ sơ của người dùng đang đăng nhập.  
**Phân quyền:** USER (đã đăng nhập)

**Headers:**

```
Authorization: Bearer <jwt_token>
```

**Postman Sample:**

```
GET http://localhost:8080/api/users/me
```

---

### `PUT /users/me`

**Tác dụng:** Cập nhật thông tin hồ sơ cá nhân (họ tên, email, SĐT, giới tính, ngày sinh, ảnh đại diện).  
**Phân quyền:** USER (đã đăng nhập)

**Điều kiện:**

- `fullName`: bắt buộc, tối đa 150 ký tự
- `email` hoặc `phone`: ít nhất một trong hai phải có
- `dateOfBirth`: phải là ngày trong quá khứ

**Body mẫu:**

```json
{
  "fullName": "Nguyễn Văn An",
  "email": "an.nguyen@gmail.com",
  "phone": "+84901234567",
  "displayName": "An Nguyen",
  "gender": "MALE",
  "dateOfBirth": "1995-06-15",
  "avatarUrl": "https://example.com/avatar-new.jpg"
}
```

---

## 4. 👥 Admin — Quản lý người dùng

> ⚠️ Tất cả các API trong phần này đều yêu cầu `ROLE: ADMIN`.

### `POST /users`

**Tác dụng:** Admin tạo mới một tài khoản người dùng, có thể gán role và trạng thái ngay khi tạo.  
**Phân quyền:** ADMIN

**Điều kiện:**

- `fullName`, `passwordHash`, `role`: bắt buộc
- `role`: `USER` | `ADMIN` | `STAFF`
- `status`: `ACTIVE` | `INACTIVE` | `BANNED` (mặc định `ACTIVE`)
- `memberLevel`: `BRONZE` | `SILVER` | `GOLD` | `PLATINUM` (mặc định `BRONZE`)
- `loyaltyPoints`, `totalSpent`: >= 0

**Body mẫu:**

```json
{
  "fullName": "Trần Thị Bình",
  "email": "binh.tran@company.com",
  "phone": "+84912345678",
  "passwordHash": "StaffPass@456",
  "role": "STAFF",
  "status": "ACTIVE",
  "displayName": "Bình Staff",
  "gender": "FEMALE",
  "dateOfBirth": "1992-03-20",
  "memberLevel": "SILVER",
  "loyaltyPoints": 500,
  "totalSpent": 5000000
}
```

---

### `GET /users`

**Tác dụng:** Admin lấy danh sách người dùng có phân trang và bộ lọc.  
**Phân quyền:** ADMIN

**Query Params:**

| Param         | Type   | Mặc định    | Mô tả                                     |
| ------------- | ------ | ----------- | ----------------------------------------- |
| `page`        | int    | 0           | Trang hiện tại (bắt đầu từ 0)             |
| `size`        | int    | 10          | Số item/trang (1–100)                     |
| `keyword`     | string | —           | Tìm theo tên, email, SĐT                  |
| `status`      | string | —           | `ACTIVE` / `INACTIVE` / `BANNED`          |
| `role`        | string | —           | `USER` / `ADMIN` / `STAFF`                |
| `memberLevel` | string | —           | `BRONZE` / `SILVER` / `GOLD` / `PLATINUM` |
| `sortBy`      | string | `createdAt` | Trường sắp xếp                            |
| `sortDir`     | string | `desc`      | `asc` hoặc `desc`                         |

**Postman Sample:**

```
GET http://localhost:8080/api/users?page=0&size=10&keyword=nguyen&status=ACTIVE&role=USER&sortBy=createdAt&sortDir=desc
```

---

### `GET /users/{id}`

**Tác dụng:** Admin lấy thông tin chi tiết một người dùng theo UUID.  
**Phân quyền:** ADMIN

**Postman Sample:**

```
GET http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000
```

---

### `PUT /users/{id}`

**Tác dụng:** Admin cập nhật đầy đủ thông tin của một user (kể cả role, status, loyalty points...).  
**Phân quyền:** ADMIN

**Điều kiện:**

- `fullName`, `role`, `status`, `memberLevel`: bắt buộc
- `role`: `USER` | `ADMIN` | `STAFF`
- `status`: `ACTIVE` | `INACTIVE` | `BANNED`
- `memberLevel`: `BRONZE` | `SILVER` | `GOLD` | `PLATINUM`

**Body mẫu:**

```json
{
  "fullName": "Trần Thị Bình Updated",
  "email": "binh.updated@company.com",
  "phone": "+84912345678",
  "passwordHash": "NewPass@789",
  "role": "USER",
  "status": "ACTIVE",
  "displayName": "Bình Updated",
  "gender": "FEMALE",
  "dateOfBirth": "1992-03-20",
  "memberLevel": "GOLD",
  "loyaltyPoints": 1500,
  "totalSpent": 15000000
}
```

---

### `PATCH /users/{id}/deactivate`

**Tác dụng:** Admin vô hiệu hóa (deactivate) một tài khoản người dùng.  
**Phân quyền:** ADMIN

**Postman Sample:**

```
PATCH http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000/deactivate
```

> Không cần body.

---

## 5. 🗺️ Destinations — Điểm đến (Public + User)

### `GET /destinations`

**Tác dụng:** Lấy danh sách các điểm đến đã được phê duyệt (`APPROVED`), hỗ trợ tìm kiếm và lọc.  
**Phân quyền:** PUBLIC

**Query Params:**

| Param        | Type    | Mặc định | Mô tả                     |
| ------------ | ------- | -------- | ------------------------- |
| `keyword`    | string  | —        | Tìm theo tên/mô tả        |
| `province`   | string  | —        | Lọc theo tỉnh/thành       |
| `region`     | string  | —        | Lọc theo vùng miền        |
| `crowdLevel` | string  | —        | `LOW` / `MEDIUM` / `HIGH` |
| `isFeatured` | boolean | —        | Điểm đến nổi bật          |
| `page`       | int     | 0        | Trang                     |
| `size`       | int     | 10       | Kích thước trang          |
| `sortBy`     | string  | `name`   | Trường sắp xếp            |
| `sortDir`    | string  | `asc`    | `asc` / `desc`            |

**Postman Sample:**

```
GET http://localhost:8080/api/destinations?keyword=Hà+Nội&province=Hà Nội&page=0&size=10
```

---

### `GET /destinations/{uuid}`

**Tác dụng:** Lấy thông tin chi tiết một điểm đến đã được phê duyệt (kèm media, ẩm thực, hoạt động, sự kiện...).  
**Phân quyền:** PUBLIC

**Postman Sample:**

```
GET http://localhost:8080/api/destinations/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

### `POST /destinations/propose`

**Tác dụng:** Người dùng đề xuất thêm điểm đến mới. Điểm đến cần được admin phê duyệt trước khi hiển thị.  
**Phân quyền:** USER (đã đăng nhập)

**Điều kiện:**

- `code`: bắt buộc, tối đa 30 ký tự
- `name`: bắt buộc, tối đa 200 ký tự
- `province`: bắt buộc
- `bestTimeFromMonth` / `bestTimeToMonth`: 1–12
- `crowdLevelDefault`: `LOW` | `MEDIUM` | `HIGH`

**Body mẫu:**

```json
{
  "code": "HN-001",
  "name": "Hồ Hoàn Kiếm",
  "slug": "ho-hoan-kiem",
  "countryCode": "VN",
  "province": "Hà Nội",
  "district": "Hoàn Kiếm",
  "region": "Miền Bắc",
  "address": "Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội",
  "latitude": 21.0285,
  "longitude": 105.8542,
  "shortDescription": "Hồ Hoàn Kiếm là trái tim của Hà Nội",
  "description": "Hồ Hoàn Kiếm (Hồ Gươm) là hồ nước ngọt nằm giữa lòng Hà Nội...",
  "bestTimeFromMonth": 3,
  "bestTimeToMonth": 5,
  "crowdLevelDefault": "HIGH",
  "isFeatured": true,
  "isActive": true,
  "isOfficial": false,
  "mediaList": [
    {
      "mediaUrl": "https://example.com/hoan-kiem.jpg",
      "mediaType": "IMAGE",
      "caption": "Hồ Hoàn Kiếm nhìn từ trên cao",
      "isPrimary": true
    }
  ],
  "foods": [
    {
      "name": "Phở Hà Nội",
      "description": "Phở bò truyền thống Hà Nội",
      "priceRange": "30.000-60.000 VNĐ",
      "imageUrl": "https://example.com/pho.jpg"
    }
  ],
  "activities": [
    {
      "name": "Dạo quanh hồ",
      "description": "Tản bộ quanh Hồ Gươm vào buổi sáng",
      "durationHours": 1,
      "isFree": true
    }
  ],
  "tips": [
    {
      "content": "Nên đến vào buổi sáng sớm để tránh đông đúc",
      "category": "TIME"
    }
  ],
  "events": [
    {
      "name": "Lễ hội phố đi bộ",
      "description": "Phố đi bộ Hoàn Kiếm mở cuối tuần",
      "month": 12
    }
  ]
}
```

---

## 6. 🛠️ Admin Destinations — Quản lý điểm đến (Admin)

> ⚠️ Tất cả API trong phần này yêu cầu `ROLE: ADMIN`.

### `GET /admin/destinations`

**Tác dụng:** Admin lấy toàn bộ danh sách điểm đến (bao gồm cả đề xuất chờ duyệt, đã từ chối...).  
**Phân quyền:** ADMIN

**Query Params:** Giống `/destinations` nhưng thêm `status`:

| Param    | Giá trị                             | Mô tả                     |
| -------- | ----------------------------------- | ------------------------- |
| `status` | `PENDING` / `APPROVED` / `REJECTED` | Lọc theo trạng thái duyệt |

**Postman Sample:**

```
GET http://localhost:8080/api/admin/destinations?status=PENDING&page=0&size=10
```

---

### `GET /admin/destinations/{uuid}`

**Tác dụng:** Admin xem chi tiết một điểm đến bất kể trạng thái.  
**Phân quyền:** ADMIN

**Postman Sample:**

```
GET http://localhost:8080/api/admin/destinations/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

### `POST /admin/destinations`

**Tác dụng:** Admin tạo mới một điểm đến chính thức (không cần chờ duyệt). Trả HTTP 201.  
**Phân quyền:** ADMIN

**Body mẫu:** _(Giống body `POST /destinations/propose` nhưng `isOfficial: true`)_

```json
{
  "code": "DN-001",
  "name": "Bà Nà Hills",
  "slug": "ba-na-hills",
  "countryCode": "VN",
  "province": "Đà Nẵng",
  "district": "Hòa Vang",
  "region": "Miền Trung",
  "address": "Thôn An Sơn, xã Hòa Ninh, Hòa Vang, Đà Nẵng",
  "latitude": 15.9994,
  "longitude": 107.9884,
  "shortDescription": "Khu du lịch nổi tiếng trên đỉnh núi Bà Nà",
  "description": "Bà Nà Hills (núi Chúa) là khu du lịch chủ đề lớn nhất Đà Nẵng...",
  "bestTimeFromMonth": 2,
  "bestTimeToMonth": 8,
  "crowdLevelDefault": "HIGH",
  "isFeatured": true,
  "isActive": true,
  "isOfficial": true,
  "mediaList": [
    {
      "mediaUrl": "https://example.com/ba-na.jpg",
      "mediaType": "IMAGE",
      "caption": "Cầu vàng Bà Nà Hills",
      "isPrimary": true
    }
  ]
}
```

---

### `PUT /admin/destinations/{uuid}`

**Tác dụng:** Admin cập nhật toàn bộ thông tin điểm đến.  
**Phân quyền:** ADMIN

**Postman Sample:** _(Body tương tự POST, gửi lên với uuid trong path)_

```
PUT http://localhost:8080/api/admin/destinations/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

### `DELETE /admin/destinations/{uuid}`

**Tác dụng:** Admin xóa một điểm đến. Trả HTTP 204.  
**Phân quyền:** ADMIN

**Postman Sample:**

```
DELETE http://localhost:8080/api/admin/destinations/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

> Không cần body. Nếu thành công sẽ không có dữ liệu trả về (204 No Content).

---

### `PATCH /admin/destinations/{uuid}/approve`

**Tác dụng:** Admin phê duyệt một đề xuất điểm đến từ người dùng (chuyển trạng thái sang `APPROVED`).  
**Phân quyền:** ADMIN

**Postman Sample:**

```
PATCH http://localhost:8080/api/admin/destinations/3fa85f64-5717-4562-b3fc-2c963f66afa6/approve
```

> Không cần body.

---

### `PATCH /admin/destinations/{uuid}/reject`

**Tác dụng:** Admin từ chối một đề xuất điểm đến, kèm lý do từ chối.  
**Phân quyền:** ADMIN

**Điều kiện:**

- `reason`: bắt buộc

**Body mẫu:**

```json
{
  "reason": "Điểm đến này đã tồn tại trong hệ thống với tên khác. Vui lòng kiểm tra lại trước khi đề xuất."
}
```

---

## 7. ❤️ Destination Follow — Theo dõi điểm đến

> ⚠️ Tất cả API trong phần này yêu cầu đăng nhập.

### `POST /destinations/{uuid}/follow`

**Tác dụng:** Người dùng theo dõi một điểm đến để nhận thông báo sự kiện, voucher, tour mới, mùa đẹp...  
**Phân quyền:** USER

**Body mẫu:** _(optional — có thể gửi trống)_

```json
{
  "notifyEvent": true,
  "notifyVoucher": true,
  "notifyNewTour": true,
  "notifyBestSeason": false
}
```

---

### `DELETE /destinations/{uuid}/follow`

**Tác dụng:** Người dùng hủy theo dõi điểm đến.  
**Phân quyền:** USER

**Postman Sample:**

```
DELETE http://localhost:8080/api/destinations/3fa85f64-5717-4562-b3fc-2c963f66afa6/follow
```

> Không cần body.

---

### `PUT /destinations/{uuid}/follow/settings`

**Tác dụng:** Cập nhật tùy chọn thông báo cho điểm đến đang theo dõi.  
**Phân quyền:** USER

**Body mẫu:**

```json
{
  "notifyEvent": false,
  "notifyVoucher": true,
  "notifyNewTour": true,
  "notifyBestSeason": true
}
```

---

### `GET /destinations/me/follows`

**Tác dụng:** Lấy danh sách các điểm đến mà người dùng hiện tại đang theo dõi.  
**Phân quyền:** USER

**Query Params:**

| Param  | Mặc định | Mô tả            |
| ------ | -------- | ---------------- |
| `page` | 0        | Trang            |
| `size` | 10       | Kích thước trang |

**Postman Sample:**

```
GET http://localhost:8080/api/destinations/me/follows?page=0&size=10
```

---

## 8. 🗓️ Tours — Quản lý tour du lịch

### `GET /tours`

**Tác dụng:** Lấy danh sách các tour du lịch có phân trang, hỗ trợ tìm kiếm theo tên và điểm đến.  
**Phân quyền:** PUBLIC

**Query Params:**

| Param           | Type   | Mặc định | Mô tả                |
| --------------- | ------ | -------- | -------------------- |
| `keyword`       | string | —        | Tìm theo tên/mã tour |
| `destinationId` | Long   | —        | Lọc theo ID điểm đến |
| `page`          | int    | 0        | Trang                |
| `size`          | int    | 10       | Kích thước trang     |

**Postman Sample:**

```
GET http://localhost:8080/api/tours?keyword=Đà+Nẵng&page=0&size=10
```

---

### `GET /tours/{id}`

**Tác dụng:** Lấy thông tin chi tiết một tour theo ID.  
**Phân quyền:** PUBLIC

**Postman Sample:**

```
GET http://localhost:8080/api/tours/1
```

---

### `POST /admin/tours`

**Tác dụng:** Admin tạo mới một tour du lịch.  
**Phân quyền:** ADMIN

**Body mẫu:**

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
  "highlights": "Khám phá Cầu Vàng, Làng Pháp, vui chơi Fantasy Park",
  "inclusions": "Xe đưa đón, cáp treo, vé vào cửa, ăn sáng",
  "exclusions": "Chi phí cá nhân, các dịch vụ bổ sung",
  "notes": "Mang theo giày thể thao, áo ấm",
  "isFeatured": true,
  "status": "ACTIVE"
}
```

---

### `PUT /admin/tours/{id}`

**Tác dụng:** Admin cập nhật thông tin tour.  
**Phân quyền:** ADMIN

**Postman Sample:** _(Body tương tự POST)_

```
PUT http://localhost:8080/api/admin/tours/1
```

---

### `DELETE /admin/tours/{id}`

**Tác dụng:** Admin xóa một tour.  
**Phân quyền:** ADMIN

**Postman Sample:**

```
DELETE http://localhost:8080/api/admin/tours/1
```

> Không cần body.

---

## 9. 📋 Bookings — Đặt tour

### `POST /bookings`

**Tác dụng:** Tạo đơn đặt tour mới. Người dùng chọn tour, lịch khởi hành, điền thông tin liên hệ và danh sách hành khách.  
**Phân quyền:** PUBLIC _(nhưng thực tế nên đăng nhập)_

**Điều kiện:**

- `userId`, `tourId`, `scheduleId`: bắt buộc
- `contactName`, `contactPhone`: bắt buộc
- `adults`: tối thiểu 1
- `passengerType`: `adult` | `child` | `infant` | `senior`

**Body mẫu:**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "tourId": 1,
  "scheduleId": 5,
  "contactName": "Nguyễn Văn An",
  "contactPhone": "+84901234567",
  "contactEmail": "an.nguyen@gmail.com",
  "adults": 2,
  "children": 1,
  "infants": 0,
  "seniors": 0,
  "passengers": [
    {
      "fullName": "Nguyễn Văn An",
      "passengerType": "adult",
      "gender": "MALE",
      "dateOfBirth": "1995-06-15",
      "identityNo": "001095012345",
      "phone": "+84901234567",
      "email": "an.nguyen@gmail.com"
    },
    {
      "fullName": "Trần Thị Bình",
      "passengerType": "adult",
      "gender": "FEMALE",
      "dateOfBirth": "1997-09-20",
      "identityNo": "001097067890",
      "phone": "+84912345678"
    },
    {
      "fullName": "Nguyễn An Khang",
      "passengerType": "child",
      "gender": "MALE",
      "dateOfBirth": "2018-03-10"
    }
  ]
}
```

---

### `GET /bookings/{id}`

**Tác dụng:** Lấy thông tin chi tiết một booking theo ID.  
**Phân quyền:** PUBLIC _(thực tế nên kiểm tra quyền)_

**Postman Sample:**

```
GET http://localhost:8080/api/bookings/1
```

---

## 10. 💳 Payments — Thanh toán

### `POST /payments`

**Tác dụng:** Tạo giao dịch thanh toán cho một booking. Có thể thanh toán bằng nhiều phương thức (CASH, BANK*TRANSFER, VNPAY, MOMO...).  
**Phân quyền:** PUBLIC *(thực tế nên đăng nhập)\_

**Điều kiện:**

- `bookingId`, `paymentMethod`, `amount`: bắt buộc

**Body mẫu:**

```json
{
  "bookingId": 1,
  "paymentMethod": "VNPAY",
  "provider": "VNPay",
  "transactionRef": "VNPAY-TXN-20260411-001",
  "amount": 3000000
}
```

> 💡 Giá trị `paymentMethod` thường gặp: `CASH`, `BANK_TRANSFER`, `VNPAY`, `MOMO`, `ZALOPAY`

---

### `GET /payments/{id}`

**Tác dụng:** Lấy thông tin chi tiết một giao dịch thanh toán theo ID.  
**Phân quyền:** PUBLIC _(thực tế nên kiểm tra quyền)_

**Postman Sample:**

```
GET http://localhost:8080/api/payments/1
```

---

## 11. 💸 Refunds — Hoàn tiền

### `POST /refunds`

**Tác dụng:** Người dùng tạo yêu cầu hoàn tiền cho một booking.  
**Phân quyền:** PUBLIC _(thực tế nên đăng nhập)_

**Điều kiện:**

- `bookingId`, `requestedAmount`: bắt buộc
- `reasonType`: loại lý do, ví dụ `CANCEL_BY_USER`, `FORCE_CANCEL`, `DUPLICATE_BOOKING`

**Body mẫu:**

```json
{
  "bookingId": 1,
  "requestedBy": "550e8400-e29b-41d4-a716-446655440000",
  "reasonType": "CANCEL_BY_USER",
  "reasonDetail": "Tôi có việc bận đột xuất không thể tham gia tour được",
  "requestedAmount": 2700000
}
```

---

### `GET /refunds/{id}`

**Tác dụng:** Lấy thông tin chi tiết một yêu cầu hoàn tiền theo ID.  
**Phân quyền:** PUBLIC _(thực tế nên kiểm tra quyền)_

**Postman Sample:**

```
GET http://localhost:8080/api/refunds/1
```

---

### `PATCH /refunds/{id}/approve`

**Tác dụng:** Admin/Staff phê duyệt yêu cầu hoàn tiền, có thể điều chỉnh số tiền hoàn lại.  
**Phân quyền:** Thực tế nên ADMIN/STAFF

**Body mẫu:**

```json
{
  "approvedAmount": 2500000
}
```

---

## 📌 Tổng hợp nhanh tất cả API

| #   | Method | Endpoint                               | Auth   | Mô tả                    |
| --- | ------ | -------------------------------------- | ------ | ------------------------ |
| 1   | GET    | `/system/health`                       | Public | Health check             |
| 2   | POST   | `/auth/register`                       | Public | Đăng ký                  |
| 3   | POST   | `/auth/login`                          | Public | Đăng nhập                |
| 4   | GET    | `/users/me`                            | User   | Xem hồ sơ cá nhân        |
| 5   | PUT    | `/users/me`                            | User   | Cập nhật hồ sơ           |
| 6   | POST   | `/users`                               | Admin  | Tạo user mới             |
| 7   | GET    | `/users`                               | Admin  | Danh sách users          |
| 8   | GET    | `/users/{id}`                          | Admin  | Chi tiết user            |
| 9   | PUT    | `/users/{id}`                          | Admin  | Cập nhật user            |
| 10  | PATCH  | `/users/{id}/deactivate`               | Admin  | Vô hiệu hóa user         |
| 11  | GET    | `/destinations`                        | Public | Danh sách điểm đến       |
| 12  | GET    | `/destinations/{uuid}`                 | Public | Chi tiết điểm đến        |
| 13  | POST   | `/destinations/propose`                | User   | Đề xuất điểm đến         |
| 14  | GET    | `/destinations/me/follows`             | User   | Điểm đến đang theo dõi   |
| 15  | POST   | `/destinations/{uuid}/follow`          | User   | Theo dõi điểm đến        |
| 16  | DELETE | `/destinations/{uuid}/follow`          | User   | Hủy theo dõi             |
| 17  | PUT    | `/destinations/{uuid}/follow/settings` | User   | Cài đặt thông báo follow |
| 18  | GET    | `/admin/destinations`                  | Admin  | DS điểm đến (all status) |
| 19  | GET    | `/admin/destinations/{uuid}`           | Admin  | Chi tiết điểm đến        |
| 20  | POST   | `/admin/destinations`                  | Admin  | Tạo điểm đến chính thức  |
| 21  | PUT    | `/admin/destinations/{uuid}`           | Admin  | Cập nhật điểm đến        |
| 22  | DELETE | `/admin/destinations/{uuid}`           | Admin  | Xóa điểm đến             |
| 23  | PATCH  | `/admin/destinations/{uuid}/approve`   | Admin  | Phê duyệt đề xuất        |
| 24  | PATCH  | `/admin/destinations/{uuid}/reject`    | Admin  | Từ chối đề xuất          |
| 25  | GET    | `/tours`                               | Public | Danh sách tour           |
| 26  | GET    | `/tours/{id}`                          | Public | Chi tiết tour            |
| 27  | POST   | `/admin/tours`                         | Admin  | Tạo tour                 |
| 28  | PUT    | `/admin/tours/{id}`                    | Admin  | Cập nhật tour            |
| 29  | DELETE | `/admin/tours/{id}`                    | Admin  | Xóa tour                 |
| 30  | POST   | `/bookings`                            | Public | Tạo booking              |
| 31  | GET    | `/bookings/{id}`                       | Public | Chi tiết booking         |
| 32  | POST   | `/payments`                            | Public | Tạo thanh toán           |
| 33  | GET    | `/payments/{id}`                       | Public | Chi tiết thanh toán      |
| 34  | POST   | `/refunds`                             | Public | Tạo yêu cầu hoàn tiền    |
| 35  | GET    | `/refunds/{id}`                        | Public | Chi tiết hoàn tiền       |
| 36  | PATCH  | `/refunds/{id}/approve`                | Admin  | Phê duyệt hoàn tiền      |

---

## 🔢 Enum Values tham khảo

| Enum                | Các giá trị                                           |
| ------------------- | ----------------------------------------------------- |
| `Gender`            | `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY`        |
| `Role`              | `USER`, `ADMIN`, `STAFF`                              |
| `Status`            | `ACTIVE`, `INACTIVE`, `BANNED`                        |
| `MemberLevel`       | `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`                |
| `CrowdLevel`        | `LOW`, `MEDIUM`, `HIGH`                               |
| `DestinationStatus` | `PENDING`, `APPROVED`, `REJECTED`                     |
| `passengerType`     | `adult`, `child`, `infant`, `senior`                  |
| `paymentMethod`     | `CASH`, `BANK_TRANSFER`, `VNPAY`, `MOMO`, `ZALOPAY`   |
| `reasonType`        | `CANCEL_BY_USER`, `FORCE_CANCEL`, `DUPLICATE_BOOKING` |
