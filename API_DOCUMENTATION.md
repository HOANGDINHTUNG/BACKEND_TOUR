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

| Role code        | Scope      | Mô tả                                        |
| ---------------- | ---------- | -------------------------------------------- |
| `SUPER_ADMIN`    | SYSTEM     | Toàn quyền hệ thống                          |
| `ADMIN`          | BACKOFFICE | Quản trị vận hành                            |
| `CONTENT_EDITOR` | BACKOFFICE | Quản lý nội dung destination, tour, media    |
| `FIELD_STAFF`    | BACKOFFICE | Nhân sự thực địa, cập nhật dữ liệu, check-in |
| `OPERATOR`       | BACKOFFICE | Điều phối schedule, booking, refund, support |
| `USER`           | CUSTOMER   | Khách hàng sử dụng ứng dụng                  |

### 1.3 Lưu ý cho role tùy biến

- Tài liệu này ưu tiên ghi theo `permission` của API.
- Nếu bạn tạo thêm role mới, role đó vẫn gọi được API nếu được gán đúng permission.
- Vì vậy không nên đọc tài liệu theo kiểu "API này chỉ ADMIN", mà nên đọc theo cột `Permission`.

### 1.4 Quy ước trong tài liệu này

| Mục                   | Nghĩa                 |
| --------------------- | --------------------- |
| `PUBLIC`              | Không cần token       |
| `AUTHENTICATED`       | Chỉ cần đăng nhập     |
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

#### `GET /users/me/preferences`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns current user's preference record
- If the user has no saved record yet, backend returns a default virtual response with empty lists and default boolean flags

#### `PUT /users/me/preferences`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`

**Request**

```json
{
  "budgetLevel": "medium",
  "preferredTripMode": "private",
  "travelStyle": "food",
  "preferredDepartureCity": "Ho Chi Minh City",
  "favoriteRegions": ["South", "Central"],
  "favoriteTags": ["food", "culture"],
  "favoriteDestinations": ["Can Tho", "Hue"],
  "prefersLowMobility": false,
  "prefersFamilyFriendly": true,
  "prefersStudentBudget": false,
  "prefersWeatherAlert": true,
  "prefersPromotionAlert": false
}
```

**Rules**

- `budgetLevel`: `low`, `medium`, `high`, `luxury`
- `preferredTripMode`: `group`, `private`, `shared`
- `travelStyle`: `relax`, `adventure`, `checkin`, `family`, `culture`, `food`, `spiritual`, `mixed`
- `preferredDepartureCity`: optional, max 120
- `favoriteRegions`, `favoriteTags`, `favoriteDestinations`: optional string arrays; blank items are dropped and duplicate items are removed while preserving order
- `prefersLowMobility`, `prefersFamilyFriendly`, `prefersStudentBudget`: default to `false` when omitted
- `prefersWeatherAlert`, `prefersPromotionAlert`: default to `true` when omitted
- Endpoint behaves as idempotent upsert by `user_id`

#### `GET /users/me/devices`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns active devices of the current user ordered by `lastSeenAt desc, id desc`

#### `POST /users/me/devices`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`

**Request**

```json
{
  "platform": "android",
  "deviceName": "Pixel 8",
  "pushToken": "push-token-abc",
  "appVersion": "1.2.0"
}
```

**Rules**

- `platform`: required, max 30, normalized to lowercase before save
- `deviceName`: optional, max 100
- `pushToken`: optional
- `appVersion`: optional, max 30
- At least one of `deviceName` or `pushToken` must be provided
- If the same current user re-registers the same `pushToken`, backend updates and reactivates the existing device instead of creating a duplicate
- `lastSeenAt` is refreshed on every successful register/upsert

#### `DELETE /users/me/devices/{id}`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Soft deletes the device by setting `isActive = false`

#### `GET /users/me/addresses`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns current user's addresses ordered by `isDefault desc, id asc`

#### `POST /users/me/addresses`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`

**Request**

```json
{
  "contactName": "Nguyen Van A",
  "contactPhone": "0901222333",
  "province": "Da Nang",
  "district": "Hai Chau",
  "ward": "Thach Thang",
  "addressLine": "123 Tran Phu",
  "isDefault": true
}
```

**Rules**

- `contactName`: required, max 150
- `contactPhone`: required, max 20, normalized before save
- `province`, `district`, `ward`: optional, max 100
- `addressLine`: required
- First address is forced to default even if `isDefault = false`
- If `isDefault = true`, other addresses of the same user are cleared from default

#### `PUT /users/me/addresses/{id}`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`

**Rules**

- Only the current owner can update the address
- `isDefault = true` promotes this address and clears other defaults
- Default address cannot be unset directly with `isDefault = false`

#### `PATCH /users/me/addresses/{id}/default`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Marks the selected address as the only default address of the current user

#### `DELETE /users/me/addresses/{id}`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Hard deletes the address
- If the deleted address was default, the next address by smallest `id` is auto-promoted as default

#### `GET /users/me/wishlist/tours`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns the current user's wishlist ordered by `created_at desc`
- Response uses a lightweight tour summary instead of the full `TourResponse`
- Backend only returns tours that are still `active` and not soft-deleted

#### `POST /users/me/wishlist/tours/{tourId}`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Adds the selected tour to the current user's wishlist
- `tourId` must reference an `active` and non-deleted tour
- This endpoint is idempotent: if the wishlist row already exists, backend returns the existing row instead of creating a duplicate

#### `DELETE /users/me/wishlist/tours/{tourId}`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Removes the selected tour from the current user's wishlist
- This endpoint is idempotent: deleting a non-existing wishlist row still returns success

#### `GET /users/me/tour-views`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns recent viewed-tour history for the current user
- Backend keeps only the latest row per `tourId` in the response and sorts by `viewed_at desc`
- Response only exposes tours that are still `active` and not soft-deleted

#### `POST /notifications`

- Permission: `user.update`
- Current foundation only supports one immediate `in_app` notification per request
- `userId` must be a valid UUID of an existing, non-deleted user
- `channel` defaults to `IN_APP`
- Future scheduling is not supported yet
- `payload`, if present, must be valid JSON

**Request**

```json
{
  "userId": "f7f4dd5f-1e0a-4cd7-b0eb-66a84f3f7f34",
  "notificationType": "PROMOTION",
  "channel": "IN_APP",
  "title": "Voucher moi",
  "body": "Ban vua nhan duoc uu dai 20%",
  "referenceType": "voucher",
  "referenceId": 15,
  "payload": "{\"voucherCode\":\"SPRING20\"}"
}
```

#### `GET /users/me/notifications`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns visible in-app notifications of the current user
- Result is sorted unread first, then `sentAt desc`, then `createdAt desc`
- Current foundation only exposes notifications that are already visible:
  - `sentAt <= now`
  - or `sentAt is null` and `scheduledAt <= now`

#### `PATCH /users/me/notifications/{id}/read`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Marks one visible notification as read for the current user
- This endpoint is idempotent: if the notification is already read, backend returns the current row unchanged

#### `PATCH /users/me/notifications/read-all`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Marks all visible unread notifications as read for the current user
- Response returns `updatedCount`

#### `POST /users/me/recommendations/tours`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Generates personalized tour recommendations for the current user and persists one `recommendation_logs` row
- If `requestedBudget` or `requestedTripMode` is omitted, backend falls back to `user_preferences` when available
- If `requestedTag` is omitted, backend falls back to the first normalized favorite tag from `user_preferences.favoriteTags`
- Current personalization foundation scores active tours using:
  - requested tag + favorite tag affinity
  - requested/preferred trip mode
  - requested/preferred budget band
  - requested people count vs tour group-size range
  - requested departure month vs `tour_seasonality`
  - user preference flags such as family-friendly, student-budget, low-mobility
  - destination affinity from `wishlist_tours` and `user_tour_views`
  - light popularity/quality boost from featured flag, rating, total bookings

**Request**

```json
{
  "requestedTag": "beach",
  "requestedBudget": "medium",
  "requestedTripMode": "group",
  "requestedPeopleCount": 4,
  "requestedDepartureAt": "2026-06-20T08:00:00",
  "size": 5
}
```

**Rules**

- `requestedTag`: optional, max `100`
- `requestedBudget`: optional, one of `low|medium|high|luxury`
- `requestedTripMode`: optional, one of `group|private|shared`
- `requestedPeopleCount`: optional, if present must be `>= 1`
- `requestedDepartureAt`: optional ISO datetime
- `size`: optional, default `10`, range `1..20`

#### `GET /users/me/recommendations/logs`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns recommendation history of the current user ordered by `createdAt desc`
- Each item returns the persisted request context plus the generated recommendation list snapshot from `recommendation_logs.generated_result`

#### `POST /users/me/support/sessions`

- Permission: `support.reply`
- Creates one support session for the current user and immediately persists the first customer message
- Current foundation requires a non-empty initial message

**Request**

```json
{
  "initialMessage": "Toi can ho tro doi lich tour",
  "attachmentUrl": "https://example.com/screenshot.png"
}
```

#### `GET /users/me/support/sessions`

- Permission: `support.view`
- Returns support sessions of the current user ordered by `updatedAt desc`

#### `GET /users/me/support/sessions/{id}`

- Permission: `support.view`
- Returns one support session of the current user together with message history ordered by `createdAt asc`

#### `POST /users/me/support/sessions/{id}/messages`

- Permission: `support.reply`
- Sends one customer message to an existing support session
- Backend rejects replies when the session is already `resolved` or `closed`
- After a customer message, session status is moved to `waiting_staff`
- `PATCH /support/sessions/{id}/rate`: user record rating (1-5) and feedback for a resolved session.

#### `GET /admin/support/sessions`

- Permission: `support.view`
- Returns support sessions, supports advanced filtering and pagination.
- Query params: `status`, `userId`, `assignedStaffId`, `page`, `size`, `sort`.

#### `GET /support/sessions/{id}`

- Permission: `support.view`
- Returns one support session with message history

#### `PATCH /support/sessions/{id}/assign`

- Permission: `support.assign`
- Assigns or unassigns one support session to a staff user
- `assignedStaffId` is optional; if omitted or blank, backend clears current assignment
- If present, `assignedStaffId` must be a valid UUID of an active internal/backoffice user

**Request**

```json
{
  "assignedStaffId": "0a0d3b88-6a0e-4bde-bc1d-4c7a87a1d5ff"
}
```

#### `PATCH /support/sessions/{id}/status`

- Permission: `support.assign`
- Updates support session status
- If status becomes `resolved` or `closed`, backend sets `endedAt`
- If status is moved back to `open`, `waiting_staff`, or `waiting_customer`, backend clears `endedAt`

**Request**

```json
{
  "status": "resolved"
}
```

#### `POST /support/sessions/{id}/messages`

- Permission: `support.reply`
- Sends one staff reply to a support session
- Backend rejects replies when the session is already `resolved` or `closed`
- If the session has no assigned staff yet, backend auto-assigns the current staff user
- After a staff reply, session status is moved to `waiting_customer`

### Schedule Chat APIs

#### `GET /schedules/{scheduleId}/chat-room`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns the chat room snapshot of one tour schedule for the current user
- Backend auto-bootstraps a room with default settings if the schedule has no chat room yet
- Current user must either:
  - be backoffice
  - or own at least one booking of the schedule in `confirmed|checked_in|completed`
- If the room visibility is `staff_only`, non-backoffice users are rejected
- If the room is inactive, non-backoffice users are rejected
- On successful access, backend auto-syncs eligible booked users into `schedule_chat_room_members` and ensures the current caller has a member row

#### `GET /schedules/{scheduleId}/chat-room/messages`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns schedule chat message history ordered by `createdAt asc`
- Reuses the same room access rules as `GET /schedules/{scheduleId}/chat-room`
- Response is paginated (`PageResponse<ScheduleChatMessageResponse>`).
- Supports `page`, `size`, `sort` query params.

#### `POST /schedules/{scheduleId}/chat-room/messages`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Sends one message into the schedule chat room as the current user

**Request**

```json
{
  "messageText": "Toi da toi diem hen",
  "attachmentUrl": "https://example.com/checkin-photo.jpg"
}
```

**Rules**

- `messageText`: required after trim, max `5000`
- `attachmentUrl`: optional, max `2000`
- Reuses the same room access rules as room read
- If the member row of the current user is muted, backend rejects the send action

#### `GET /admin/schedules/{scheduleId}/chat-room`

- Permission: `schedule.view`
- Returns one schedule chat room for backoffice scope
- If the schedule has no room yet, backend auto-bootstraps a default room and ensures the current backoffice caller has a member row

#### `PUT /admin/schedules/{scheduleId}/chat-room`

- Permission: `schedule.update`
- Creates or updates schedule chat room settings

**Request**

```json
{
  "roomName": "Nhom khoi hanh 30-04",
  "visibility": "staff_only",
  "isActive": true
}
```

**Rules**

- `roomName`: optional, max `200`; if blank and room is new, backend falls back to generated default name
- `visibility`: optional, one of `all_members|staff_only`
- `isActive`: optional boolean
- Audit currently records `schedule_chat_room.upsert`

#### `GET /admin/schedules/{scheduleId}/chat-room/messages`

- Permission: `schedule.view`
- Returns full message history of one schedule chat room, paginated.
- Supports `page`, `size`, `sort` query params.

#### `PATCH /admin/schedules/{scheduleId}/chat-room/members/{userId}/mute`

- Permission: `schedule.update`
- Mutes or unmutes a member in the chat room.
- Is idempotent.

#### `POST /admin/schedules/{scheduleId}/chat-room/messages`

- Permission: `schedule.update`
- Sends one backoffice message into the schedule chat room
- If the current caller has no member row yet, backend auto-creates it
- Audit currently records `schedule_chat_message.send`

### 5.2 Weather APIs

#### `GET /destinations/{destinationUuid}/weather/forecasts`

- Access: `PUBLIC`
- Returns upcoming weather forecasts of one approved, active destination
- Sort: `forecastDate asc`
- Rows with `forecastDate < today` are hidden from the public API

#### `GET /destinations/{destinationUuid}/weather/alerts`

- Access: `PUBLIC`
- Returns current active weather alerts of one approved, active destination
- Backend only exposes rows where:
  - `isActive = true`
  - `validFrom <= now`
- `validTo >= now`
- Sort: `validFrom desc`

#### `GET /destinations/{destinationUuid}/weather/crowd-predictions`

- Access: `PUBLIC`
- Returns upcoming crowd predictions of one approved, active destination
- Sort: `predictionDate asc`
- Rows with `predictionDate < today` are hidden from the public API

#### `GET /admin/destinations/{destinationUuid}/weather/forecasts`

- Permission: `destination.view`
- Returns all stored weather forecasts of one destination
- Sort: `forecastDate asc`

#### `PUT /admin/destinations/{destinationUuid}/weather/forecasts/{forecastDate}`

- Permission: `destination.update`
- Upserts one forecast row by unique key `(destination_id, forecast_date)`

**Request**

```json
{
  "weatherCode": "rain",
  "summary": "Mua nhe buoi chieu",
  "tempMin": 24.5,
  "tempMax": 31.0,
  "humidityPercent": 82.0,
  "windSpeed": 12.5,
  "rainProbability": 65.0,
  "sourceName": "manual",
  "rawPayload": "{\"provider\":\"manual\"}"
}
```

**Rules**

- `forecastDate`: required ISO date in path
- `tempMin <= tempMax` when both are present
- `humidityPercent` and `rainProbability`: `0..100`
- `windSpeed >= 0`
- `rawPayload` must be valid JSON if provided

#### `GET /admin/destinations/{destinationUuid}/weather/alerts`

- Permission: `destination.view`
- Returns all weather alerts of one destination
- Sort: `validFrom desc`

#### `POST /admin/destinations/{destinationUuid}/weather/alerts`

- Permission: `destination.update`
- Creates one weather alert for the destination

#### `PUT /admin/destinations/{destinationUuid}/weather/alerts/{alertId}`

- Permission: `destination.update`
- Full update of one weather alert

**Request**

```json
{
  "scheduleId": 101,
  "severity": "warning",
  "alertType": "heavy_rain",
  "title": "Mua lon tren hanh trinh",
  "message": "Can nhac doi gio xuat phat va mang ao mua",
  "actionAdvice": "Theo doi thong bao moi nhat tu dieu hanh",
  "validFrom": "2026-04-18T08:00:00",
  "validTo": "2026-04-18T16:00:00",
  "isActive": true
}
```

**Rules**

- `severity`: one of `info`, `watch`, `warning`, `danger`
- `alertType`, `title`, `message`: required and non-blank
- `validTo >= validFrom`
- `scheduleId` is optional
- If `scheduleId` is provided, schedule must exist and belong to a tour whose destination matches `destinationUuid`

#### `PATCH /admin/destinations/{destinationUuid}/weather/alerts/{alertId}/status`

- Permission: `destination.update`
- Toggles alert visibility without rewriting the rest of the payload

**Request**

```json
{
  "active": false
}
```

#### `GET /admin/destinations/{destinationUuid}/weather/crowd-predictions`

- Permission: `destination.view`
- Returns all stored crowd predictions of one destination
- Sort: `predictionDate asc`

#### `PUT /admin/destinations/{destinationUuid}/weather/crowd-predictions/{predictionDate}`

- Permission: `destination.update`
- Upserts one crowd prediction row by unique key `(destination_id, prediction_date)`

**Request**

```json
{
  "crowdLevel": "high",
  "predictedVisitors": 12000,
  "confidenceScore": 84.0,
  "reasonsJson": "{\"holiday\":true,\"festival\":\"fireworks\"}"
}
```

**Rules**

- `predictionDate`: required ISO date in path
- `crowdLevel`: required, one of `low|medium|high|very_high`
- `predictedVisitors >= 0` when provided
- `confidenceScore`: optional, if provided must be in range `0..100`
- `reasonsJson` must be valid JSON if provided
- Audit currently records `crowd_prediction.upsert`

#### `GET /route-estimates`

- Access: `PUBLIC`
- Returns recent route estimates sorted by `createdAt desc`
- Optional query params:
  - `fromLabel`
  - `toLabel`
- If query params are provided, backend filters by `containsIgnoreCase` on stored labels
- Current foundation returns at most `20` rows

#### `GET /admin/route-estimates`

- Permission: `destination.view`
- Returns recent route estimates sorted by `createdAt desc`
- Supports the same optional query params as public route estimate read
- Current foundation returns at most `50` rows

#### `POST /admin/route-estimates`

- Permission: `destination.update`
- Creates one route estimate snapshot

**Request**

```json
{
  "fromLabel": "Ho Chi Minh City",
  "toLabel": "Da Lat",
  "fromLatitude": 10.776889,
  "fromLongitude": 106.700806,
  "toLatitude": 11.940419,
  "toLongitude": 108.458313,
  "distanceKm": 305.5,
  "durationMinutes": 360,
  "googleMapUrl": "https://maps.google.com/example",
  "sourceName": "manual"
}
```

**Rules**

- `fromLabel`, `toLabel`: required, non-blank, max `255`
- `fromLatitude/fromLongitude` must be provided together if one is present
- `toLatitude/toLongitude` must be provided together if one is present
- `fromLatitude`, `toLatitude`: if provided, must be in range `-90..90`
- `fromLongitude`, `toLongitude`: if provided, must be in range `-180..180`
- `distanceKm >= 0` when provided
- `durationMinutes >= 0` when provided
- `googleMapUrl`: optional, max `2000`
- `sourceName`: optional, max `100`
- Audit currently records `route_estimate.create`

### 5.3 Loyalty APIs

> Luu y: codebase hien chua co permission rieng cho badge/passport admin flow, nen admin badge endpoints dang tam gate bang `user.view` va `user.update`.

#### `GET /users/me/passport`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns travel passport snapshot of the current user
- If the current user does not have a `travel_passports` row yet, backend auto-bootstraps one before returning response
- Response includes:
  - passport core stats
  - unlocked badges ordered by `unlockedAt desc`
  - visited destinations ordered by `lastVisitedAt desc`

#### `GET /users/me/checkins`

- Access: `AUTHENTICATED`
- Security: `isAuthenticated()`
- Returns check-in history of the current user ordered by `createdAt desc`
- Each row may include booking snapshot and destination snapshot when available

#### `GET /badges`

- Permission: `user.view`
- Returns badge catalog ordered by `createdAt desc`

#### `GET /badges/{id}`

- Permission: `user.view`
- Returns one badge definition

#### `POST /badges`

- Permission: `user.update`
- Creates one badge definition

**Request**

```json
{
  "code": "FIRST_TRIP",
  "name": "First Trip",
  "description": "Mo khi hoan thanh chuyen di dau tien",
  "iconUrl": "https://example.com/badges/first-trip.png",
  "conditionJson": "{\"minCompletedTours\":1}",
  "isActive": true
}
```

**Rules**

- `code`: required, max `50`, normalized uppercase
- `name`: required, max `150`
- `conditionJson` must be valid JSON if provided
- `code` must be unique
- `isActive`: optional, defaults to `true`

#### `PUT /badges/{id}`

- Permission: `user.update`
- Full update of one badge definition
- Reuses create validation, including unique `code`

#### `PATCH /badges/{id}/status`

- Permission: `user.update`
- Toggles badge active flag

**Request**

```json
{
  "isActive": false
}
```

#### `POST /badges/{badgeId}/grant/users/{userId}`

- Permission: `user.update`
- Grants one active badge to the target user's passport
- Backend auto-bootstraps the target user's passport if it does not exist yet
- This endpoint is idempotent: if the badge has already been granted to the passport, backend returns the existing `passport_badges` row

#### `POST /users/{userId}/checkins`

- Permission: `booking.checkin`
- Creates one manual check-in row for the target user
- Backend also syncs passport totals and visited-destination snapshot

**Request**

```json
{
  "bookingId": 123,
  "destinationUuid": "0d7885b7-2a0a-4bbf-b9ae-d3ae8a8c5d4a",
  "checkinLatitude": 16.047079,
  "checkinLongitude": 108.20623,
  "note": "Arrived at destination"
}
```

**Rules**

- `bookingId` and `destinationUuid` cannot both be empty
- if `bookingId` is provided, booking must belong to `userId`
- if `destinationUuid` is provided together with `bookingId`, it must match the booking's tour destination
- if `bookingId` is provided and a check-in row for `(bookingId, userId)` already exists, backend returns the existing row instead of creating a duplicate
- successful create updates:
  - `travel_passports.totalCheckins`
  - `travel_passports.totalDestinations`
  - `passport_visited_destinations`

### 5.4 RBAC Read APIs

> Luu y: current code gates these read-only RBAC endpoints with `user.view` because dedicated `role.view` / `permission.view` permissions have not been introduced yet.

#### `GET /roles`

- Permission: `user.view`
- Mo ta: Tra danh sach role cung toan bo permission gan vao moi role
- Thu tu: `hierarchyLevel desc`, `name asc`

#### `GET /roles/{id}`

- Permission: `user.view`
- Mo ta: Tra chi tiet mot role cung danh sach permission da sap xep theo `moduleName`, `actionName`, `code`

#### `GET /permissions`

- Permission: `user.view`
- Mo ta: Tra danh sach permission dang co trong he thong
- Thu tu: `moduleName asc`, `actionName asc`, `name asc`

#### `POST /roles`

- Permission: `role.assign`
- Mo ta: Tao role moi

**Request**

```json
{
  "code": "RBAC_AUDITOR",
  "name": "RBAC Auditor",
  "description": "Can review role and permission setup",
  "roleScope": "BACKOFFICE",
  "hierarchyLevel": 40,
  "isSystemRole": false,
  "isActive": true
}
```

**Rules**

- `code`: required, max 50, normalized to uppercase
- `name`: required, max 120
- `description`: optional, max 255
- `roleScope`: required, one of `SYSTEM`, `BACKOFFICE`, `CUSTOMER`
- `hierarchyLevel`: required, `0..9999`
- `isSystemRole`: optional, defaults to `false`
- `isActive`: optional, defaults to `true`
- `code` must be unique
- Only `SUPER_ADMIN` can create system roles (`roleScope = SYSTEM` or `isSystemRole = true`)

#### `PUT /roles/{id}`

- Permission: `role.assign`
- Mo ta: Cap nhat metadata cua role

**Rules**

- `code` must remain unique
- Only `SUPER_ADMIN` can modify existing system roles
- Request is treated as full update for role metadata

#### `PATCH /roles/{id}/permissions`

- Permission: `role.assign`
- Mo ta: Thay the toan bo tap permission cua role bang `permissionCodes`

**Request**

```json
{
  "permissionCodes": ["role.view", "permission.view", "audit.view"]
}
```

**Rules**

- `permissionCodes`: required array
- Blank values are removed, duplicate codes are deduplicated while preserving order
- Every permission code must exist
- Inactive permissions cannot be assigned
- Only `SUPER_ADMIN` can change permissions of system roles

### 5.5 Audit Logs

#### `GET /audit-logs`

- Permission: `audit.view`
- Mo ta: Tra danh sach audit log theo bo loc va phan trang
- Thu tu: `createdAt desc`

**Query params**

- `page`: mac dinh `0`, phai `>= 0`
- `size`: mac dinh `20`, phai trong khoang `1..100`
- `actorUserId`: UUID actor thuc hien hanh dong
- `actionName`: filter contains ignore-case
- `entityName`: filter contains ignore-case
- `entityId`: filter exact match
- `from`: ISO datetime, `createdAt >= from`
- `to`: ISO datetime, `createdAt <= to`

**Rules**

- `from` khong duoc lon hon `to`
- `oldData` va `newData` tra ve dang JSON neu parse duoc, neu khong se fallback thanh text node
- `ipAddress` va `userAgent` duoc capture tu request hien tai neu co
- O pham vi hien tai, audit producer da duoc noi vao cac admin write flow sau:
  - `role.create`
  - `role.update`
  - `permission.assign`
  - `user.create`
  - `user.update`
  - `user.deactivate`

### 5.4 Promotion Campaigns

#### `GET /promotion-campaigns`

- Permission: `voucher.view`
- Mo ta: Tra danh sach promotion campaign theo bo loc va phan trang
- Thu tu mac dinh: `createdAt desc`

**Query params**

- `page`: mac dinh `0`, phai `>= 0`
- `size`: mac dinh `10`, phai trong khoang `1..100`
- `keyword`: tim theo `code` hoac `name`
- `isActive`: filter theo trang thai active
- `targetMemberLevel`: `bronze|silver|gold|platinum|diamond`
- `startsFrom`: ISO datetime, `startAt >= startsFrom`
- `endsTo`: ISO datetime, `endAt <= endsTo`
- `sortBy`: `code|name|startAt|endAt|targetMemberLevel|isActive|createdAt|updatedAt`
- `sortDir`: `asc|desc`

**Rules**

- `startsFrom` khong duoc lon hon `endsTo`

#### `GET /promotion-campaigns/{id}`

- Permission: `voucher.view`
- Mo ta: Tra chi tiet mot promotion campaign

#### `POST /promotion-campaigns`

- Permission: `voucher.create`
- Mo ta: Tao promotion campaign moi

**Request**

```json
{
  "code": "SPRING_SALE_2026",
  "name": "Spring Sale 2026",
  "description": "Campaign cho khach hang thanh vien",
  "startAt": "2026-04-20T00:00:00",
  "endAt": "2026-04-30T23:59:00",
  "targetMemberLevel": "gold",
  "conditionsJson": {
    "minOrderAmount": 2000000
  },
  "rewardJson": {
    "discountPercent": 10
  },
  "isActive": true
}
```

**Rules**

- `code`: required, max `40`, duoc normalize uppercase
- `name`: required, max `200`
- `startAt`, `endAt`: required
- `endAt` phai sau `startAt`
- `targetMemberLevel`: optional, one of `bronze|silver|gold|platinum|diamond`
- `conditionsJson`, `rewardJson`: optional JSON payload, backend luu dang JSON string
- `isActive`: optional, defaults to `true`
- `code` must be unique

#### `PUT /promotion-campaigns/{id}`

- Permission: `voucher.update`
- Mo ta: Cap nhat full metadata cua promotion campaign

**Rules**

- Reuse toan bo validation cua create
- `code` must remain unique
- Request duoc xu ly theo full update cho campaign metadata

#### `PATCH /promotion-campaigns/{id}/status`

- Permission: `voucher.delete`
- Mo ta: Bat/tat trang thai hoat dong cua campaign

**Request**

```json
{
  "isActive": false
}
```

**Rules**

- `isActive`: required boolean
- Audit currently records:
  - `promotion_campaign.create`
  - `promotion_campaign.update`
  - `promotion_campaign.status.update`

### 5.5 Vouchers

#### `GET /vouchers`

- Permission: `voucher.view`
- Mo ta: Tra danh sach voucher theo bo loc va phan trang
- Thu tu mac dinh: `createdAt desc`

**Query params**

- `page`: mac dinh `0`, phai `>= 0`
- `size`: mac dinh `10`, phai trong khoang `1..100`
- `keyword`: tim theo `code` hoac `name`
- `campaignId`: filter theo campaign
- `discountType`: `percentage|fixed_amount|gift|cashback`
- `applicableScope`: `all|tour|destination`
- `applicableMemberLevel`: `bronze|silver|gold|platinum|diamond`
- `isActive`: filter theo trang thai active
- `activeAt`: ISO datetime, voucher co hieu luc tai thoi diem nay
- `startsFrom`: ISO datetime, `startAt >= startsFrom`
- `endsTo`: ISO datetime, `endAt <= endsTo`
- `sortBy`: `code|name|discountType|discountValue|minOrderValue|startAt|endAt|usedCount|isActive|createdAt|updatedAt`
- `sortDir`: `asc|desc`

**Rules**

- `startsFrom` khong duoc lon hon `endsTo`

#### `GET /vouchers/{id}`

- Permission: `voucher.view`
- Mo ta: Tra chi tiet mot voucher

#### `POST /vouchers`

- Permission: `voucher.create`
- Mo ta: Tao voucher moi

**Request**

```json
{
  "code": "SPRING10",
  "campaignId": 3,
  "name": "Spring 10",
  "description": "Voucher giam gia cho tour mua xuan",
  "discountType": "percentage",
  "discountValue": 10,
  "maxDiscountAmount": 500000,
  "minOrderValue": 1000000,
  "usageLimitTotal": 100,
  "usageLimitPerUser": 2,
  "applicableScope": "tour",
  "applicableTourId": 9,
  "applicableMemberLevel": "gold",
  "startAt": "2026-04-20T00:00:00",
  "endAt": "2026-04-30T23:59:00",
  "isStackable": true,
  "isActive": true
}
```

**Rules**

- `code`: required, max `50`, duoc normalize uppercase
- `name`: required, max `200`
- `discountType`: required, one of `percentage|fixed_amount|gift|cashback`
- `discountValue`: required, `>= 0`
- voucher khong phai `gift` thi `discountValue` phai `> 0`
- voucher `percentage` thi `discountValue` phai `<= 100`
- `maxDiscountAmount`: optional, neu co phai `> 0`
- `minOrderValue`: required, phai `>= 0`
- `usageLimitTotal`: optional, neu co phai `>= 1`
- `usageLimitPerUser`: optional, neu co phai `>= 1`, mac dinh `1`
- `applicableScope`: required, one of `all|tour|destination`
- neu `applicableScope = all` thi `applicableTourId` va `applicableDestinationId` phai null
- neu `applicableScope = tour` thi `applicableTourId` bat buoc ton tai va `applicableDestinationId` phai null
- neu `applicableScope = destination` thi `applicableDestinationId` bat buoc ton tai va `applicableTourId` phai null
- `campaignId`: optional, neu co thi campaign phai ton tai
- neu co `campaignId` thi khung `startAt/endAt` cua voucher phai nam trong khung campaign
- `isStackable`: optional, mac dinh `false`
- `isActive`: optional, mac dinh `true`
- `code` must be unique

#### `PUT /vouchers/{id}`

- Permission: `voucher.update`
- Mo ta: Cap nhat full metadata cua voucher

**Rules**

- Reuse toan bo validation cua create
- `code` must remain unique
- `usageLimitTotal` khong duoc nho hon `usedCount` hien tai

#### `PATCH /vouchers/{id}/status`

- Permission: `voucher.delete`
- Mo ta: Bat/tat trang thai hoat dong cua voucher

**Request**

```json
{
  "isActive": false
}
```

**Rules**

- `isActive`: required boolean
- Audit currently records:
  - `voucher.create`
  - `voucher.update`
  - `voucher.status.update`

#### `GET /users/me/vouchers`

- Permission: `isAuthenticated()`
- Mo ta: Tra danh sach voucher da claim cua user hien tai
- Thu tu mac dinh: `claimedAt desc`

**Response notes**

- moi item tra ve thong tin claim + metadata cua voucher
- `status` duoc compute tai runtime:
  - `available`
  - `inactive`
  - `expired`
  - `exhausted_total`
  - `used_up`

#### `POST /vouchers/claim`

- Permission: `isAuthenticated()`
- Mo ta: Claim mot voucher theo code cho user hien tai

**Request**

```json
{
  "voucherCode": "SPRING10"
}
```

**Rules**

- `voucherCode`: required, max `50`, backend normalize uppercase truoc khi lookup
- voucher phai ton tai
- voucher phai `isActive = true`
- current time phai nam trong khung `startAt..endAt`
- neu voucher co `applicableMemberLevel` thi phai khop member level hien tai cua user
- neu `usageLimitTotal` da het theo `usedCount` thi khong duoc claim
- moi user chi duoc claim 1 lan cho moi voucher

### 5.6 Products

> Luu y: do seed permission hien tai chua co ma rieng cho `product` va `combo`, flow nay tam thoi gate bang `voucher.view|create|update|delete`.

#### `GET /products`

- Permission: `voucher.view`
- Mo ta: Tra danh sach product theo bo loc va phan trang

**Query params**

- `page`: mac dinh `0`, phai `>= 0`
- `size`: mac dinh `10`, phai trong khoang `1..100`
- `keyword`: tim theo `sku` hoac `name`
- `productType`: `gear|insurance|food|souvenir|service|gift`
- `isGiftable`: filter boolean
- `isActive`: filter boolean
- `sortBy`: `sku|name|productType|unitPrice|stockQty|isGiftable|isActive|createdAt|updatedAt`
- `sortDir`: `asc|desc`

#### `GET /products/{id}`

- Permission: `voucher.view`
- Mo ta: Tra chi tiet mot product

#### `POST /products`

- Permission: `voucher.create`
- Mo ta: Tao product moi

**Request**

```json
{
  "sku": "SKU-001",
  "name": "Travel Kit",
  "description": "Bo do nghe du lich co ban",
  "productType": "gear",
  "unitPrice": 150000,
  "stockQty": 20,
  "isGiftable": true,
  "isActive": true
}
```

**Rules**

- `sku`: required, max `50`, duoc normalize uppercase
- `name`: required, max `200`
- `productType`: required, one of `gear|insurance|food|souvenir|service|gift`
- `unitPrice`: required, phai `>= 0`
- `stockQty`: required, phai `>= 0`
- `isGiftable`: optional, mac dinh `false`
- `isActive`: optional, mac dinh `true`
- `sku` must be unique

#### `PUT /products/{id}`

- Permission: `voucher.update`
- Mo ta: Cap nhat full metadata cua product

#### `PATCH /products/{id}/status`

- Permission: `voucher.delete`
- Mo ta: Bat/tat trang thai hoat dong cua product

**Request**

```json
{
  "isActive": false
}
```

### 5.7 Combo Packages

> Luu y: do seed permission hien tai chua co ma rieng cho `product` va `combo`, flow nay tam thoi gate bang `voucher.view|create|update|delete`.

#### `GET /combo-packages`

- Permission: `voucher.view`
- Mo ta: Tra danh sach combo package theo bo loc va phan trang

**Query params**

- `page`: mac dinh `0`, phai `>= 0`
- `size`: mac dinh `10`, phai trong khoang `1..100`
- `keyword`: tim theo `code` hoac `name`
- `isActive`: filter boolean
- `sortBy`: `code|name|basePrice|discountAmount|isActive|createdAt|updatedAt`
- `sortDir`: `asc|desc`

#### `GET /combo-packages/{id}`

- Permission: `voucher.view`
- Mo ta: Tra chi tiet combo package kem nested item list

#### `POST /combo-packages`

- Permission: `voucher.create`
- Mo ta: Tao combo package moi

**Request**

```json
{
  "code": "COMBO-001",
  "name": "Adventure Combo",
  "description": "Combo danh cho khach thich trai nghiem",
  "basePrice": 250000,
  "discountAmount": 50000,
  "isActive": true,
  "items": [
    {
      "itemType": "product",
      "itemRefId": 7,
      "itemName": "Travel Kit",
      "quantity": 1,
      "unitPrice": 150000
    },
    {
      "itemType": "tour",
      "itemRefId": 9,
      "itemName": "Island Tour Add-on",
      "quantity": 1,
      "unitPrice": 100000
    }
  ]
}
```

**Rules**

- `code`: required, max `40`, duoc normalize uppercase
- `name`: required, max `200`
- `basePrice`: required, phai `>= 0`
- `discountAmount`: required, phai `>= 0` va `<= basePrice`
- `items`: bat buoc co it nhat 1 item
- `basePrice` phai bang tong `quantity * unitPrice` cua item list
- `itemType`: hien support `product|tour|service|gift|other`
- neu `itemType = product` hoac `tour` thi `itemRefId` bat buoc ton tai
- `quantity` phai `>= 1`
- `unitPrice` phai `>= 0`
- `code` must be unique

#### `PUT /combo-packages/{id}`

- Permission: `voucher.update`
- Mo ta: Cap nhat full metadata va replace lai item list cua combo package

#### `PATCH /combo-packages/{id}/status`

- Permission: `voucher.delete`
- Mo ta: Bat/tat trang thai hoat dong cua combo package

**Request**

```json
{
  "isActive": false
}
```

### 5.8 Admin Users

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

| Param         | Type   | Default     | Ghi chú                                                                                                                                          |
| ------------- | ------ | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `page`        | int    | `0`         | >= 0                                                                                                                                             |
| `size`        | int    | `10`        | 1..100                                                                                                                                           |
| `keyword`     | string | -           | max 100                                                                                                                                          |
| `status`      | enum   | -           | `pending`, `active`, `suspended`, `blocked`, `deleted`                                                                                           |
| `roleCode`    | string | -           | role code cần lọc                                                                                                                                |
| `memberLevel` | enum   | -           | `bronze`, `silver`, `gold`, `platinum`, `diamond`                                                                                                |
| `sortBy`      | string | `createdAt` | `id`, `fullName`, `displayName`, `email`, `phone`, `userCategory`, `status`, `memberLevel`, `createdAt`, `updatedAt`, `lastLoginAt`, `deletedAt` |
| `sortDir`     | string | `desc`      | `asc` hoặc `desc`                                                                                                                                |

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

### 5.4 UserResponse shape

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

| Param        | Type    | Default |
| ------------ | ------- | ------- |
| `keyword`    | string  | -       |
| `province`   | string  | -       |
| `region`     | string  | -       |
| `crowdLevel` | enum    | -       |
| `isFeatured` | boolean | -       |
| `page`       | int     | `0`     |
| `size`       | int     | `10`    |
| `sortBy`     | string  | `name`  |
| `sortDir`    | string  | `asc`   |

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

| Param                 | Type       | Default     |
| --------------------- | ---------- | ----------- |
| `destinationId`       | long       | -           |
| `keyword`             | string     | -           |
| `tagIds`              | list<long> | -           |
| `minPrice`            | decimal    | -           |
| `maxPrice`            | decimal    | -           |
| `travelMonth`         | int        | -           |
| `featuredOnly`        | boolean    | -           |
| `studentFriendlyOnly` | boolean    | -           |
| `familyFriendlyOnly`  | boolean    | -           |
| `seniorFriendlyOnly`  | boolean    | -           |
| `difficultyLevel`     | int        | -           |
| `activityLevel`       | int        | -           |
| `minDurationDays`     | int        | -           |
| `maxDurationDays`     | int        | -           |
| `travellerAge`        | int        | -           |
| `groupSize`           | int        | -           |
| `tripMode`            | string     | -           |
| `transportType`       | string     | -           |
| `minRating`           | decimal    | -           |
| `sortBy`              | string     | `createdAt` |
| `sortDir`             | string     | `desc`      |
| `page`                | int        | `0`         |
| `size`                | int        | `10`        |

**Rules from current code**

- Public search chỉ trả các tour chưa soft-delete và có `status = active`
- `destinationId` filter theo tour destination
- `keyword` match không phân biệt hoa thường trên `name`, `slug`, `shortDescription`, `description`, `highlights`
- `tagIds` filter theo `tour_tags`; nếu không có tour nào match tag thì backend trả page rỗng
- `minPrice` / `maxPrice` filter theo `tours.base_price`; `maxPrice` không được nhỏ hơn `minPrice`
- `travelMonth` filter theo `tour_seasonality.month_from/month_to`; nếu không có tour nào match tháng thì backend trả page rỗng
- `featuredOnly=true` chỉ lấy các tour có `isFeatured = true`
- `studentFriendlyOnly=true` chỉ lấy các tour có `isStudentFriendly = true`
- `familyFriendlyOnly=true` chỉ lấy các tour có `isFamilyFriendly = true`
- `seniorFriendlyOnly=true` chỉ lấy các tour có `isSeniorFriendly = true`
- `difficultyLevel` filter theo `tours.difficulty_level` và phải nằm trong `1..5`
- `activityLevel` filter theo `tours.activity_level` và phải nằm trong `1..5`
- `minDurationDays` / `maxDurationDays` filter theo `tours.duration_days`; `maxDurationDays` không được nhỏ hơn `minDurationDays`
- `travellerAge` filter theo eligibility của `tours.min_age/max_age`; tuổi phải `>= 0`
- `groupSize` filter theo eligibility của `tours.min_group_size/max_group_size`; size phải `>= 1`
- `tripMode` chỉ nhận `group`, `private`, `shared`
- `transportType` match không phân biệt hoa thường trên `tours.transport_type`
- `minRating` filter theo `tours.average_rating`; giá trị phải nằm trong `0..5`
- `sortBy` chỉ nhận: `name`, `basePrice`, `durationDays`, `averageRating`, `totalBookings`, `createdAt`
- `sortDir` chỉ nhận `asc` hoặc `desc`
- `travelMonth` phải nằm trong khoảng `1..12`
- `page >= 0`, `1 <= size <= 100`
- `GET /tours` vẫn trả response mỏng hơn `GET /tours/{id}` và không load các collection lớn

**Request**

```http
GET http://localhost:8088/api/v1/tours?keyword=Da+Nang&destinationId=1&tagIds=4&tagIds=8&minPrice=900000&maxPrice=1500000&travelMonth=6&featuredOnly=true&familyFriendlyOnly=true&difficultyLevel=3&activityLevel=4&minDurationDays=2&maxDurationDays=5&travellerAge=18&groupSize=4&tripMode=private&transportType=car&minRating=4.5&sortBy=basePrice&sortDir=asc&page=0&size=10
```

### `GET /tours/{id}`

- Access: `PUBLIC`
- If the caller is authenticated, backend records `user_tour_views` for the visited `tourId`
- View logging uses a 30-minute cooldown per `(userId, tourId)` pair to reduce refresh spam

**Rules from current code**

- Detail hiện trả thêm các khối nội dung `media`, `itineraryDays[].items`, `checklistItems`
- `GET /tours` vẫn giữ response nhẹ hơn và không load các collection này

### `GET /tours/{id}/schedules`

- Access: `PUBLIC`

**Rules from current code**

- Public API chỉ trả các schedule chưa bị soft-delete và có status thuộc một trong các giá trị:
  - `open`
  - `closed`
  - `full`
  - `departed`
  - `completed`
- `draft` không xuất hiện ở public list

### `GET /tours/{tourId}/schedules/{scheduleId}`

- Access: `PUBLIC`
- `scheduleId` phải thuộc đúng `tourId`
- Hiện tại detail public đang dùng cùng query path với admin, nên nếu schedule tồn tại và chưa soft-delete thì API vẫn trả được detail

### `POST /admin/tours`

- Permission: `tour.create`

**Rules from current code**

- `code`, `name`, `slug`, `destinationId`, `basePrice`, `durationDays` are required
- `basePrice >= 0`
- `durationDays >= 1`
- `durationNights >= 0`
- `durationNights` must not be greater than `durationDays`
- Nếu `cancellationPolicyId` không được truyền, backend tự bind `default active cancellation policy`
- Nếu `cancellationPolicyId` được truyền, policy phải tồn tại, active, và phải có ít nhất một rule
- `tagIds` nếu có truyền thì phải unique, dương, và phải map được tới tag đang active
- `seasonality[].seasonName` phải unique trong cùng tour
- `seasonality[].monthFrom/monthTo` nếu cùng có dữ liệu thì `monthTo` không được nhỏ hơn `monthFrom`
- `seasonality[].recommendationScore >= 0`
- `media[].sortOrder` phải unique trong cùng tour
- `itineraryDays[].dayNumber` phải unique trong cùng tour và không được vượt `durationDays`
- `itineraryDays[].items[].sequenceNo` phải unique trong cùng ngày
- `itineraryDays[].items[].endTime` không được trước `startTime`
- `checklistItems[].itemName` phải unique trong cùng tour
- If `currency` is omitted, backend defaults to `VND`
- If `status` is omitted, backend defaults to `draft`
- `destinationId` must exist and must not be soft-deleted
- Backend hiện replace toàn bộ `media`, `itineraryDays/items`, `checklistItems` theo payload khi create/update

**Request**

```json
{
  "code": "TOUR-BNH-2026",
  "name": "Tour Bà Nà Hills 2 ngày 1 đêm",
  "slug": "tour-ba-na-hills-2n1d",
  "destinationId": 1,
  "cancellationPolicyId": 1,
  "tagIds": [1, 4],
  "basePrice": 1500000,
  "currency": "VND",
  "durationDays": 2,
  "durationNights": 1,
  "shortDescription": "Tour ngắn ngày cho gia đình",
  "description": "Lịch trình bao gồm cáp treo, Cầu Vàng và Fantasy Park",
  "transportType": "BUS",
  "tripMode": "GROUP",
  "highlights": "Cầu Vàng, Làng Pháp, Fantasy Park",
  "inclusions": "Xe đưa đón, cáp treo, vé vào cửa",
  "exclusions": "Chi phí cá nhân",
  "notes": "Mang giày thể thao",
  "isFeatured": true,
  "status": "ACTIVE",
  "media": [
    {
      "mediaType": "image",
      "mediaUrl": "https://cdn.example.com/tours/ba-na-cover.jpg",
      "altText": "Bà Nà cover",
      "sortOrder": 0,
      "isActive": true
    }
  ],
  "seasonality": [
    {
      "seasonName": "Mùa hè",
      "monthFrom": 5,
      "monthTo": 8,
      "recommendationScore": 9.5,
      "notes": "Thời tiết đẹp, phù hợp gia đình"
    }
  ],
  "itineraryDays": [
    {
      "dayNumber": 1,
      "title": "Khởi hành đi Bà Nà",
      "description": "Tập trung và di chuyển lên khu du lịch",
      "items": [
        {
          "sequenceNo": 1,
          "itemType": "visit",
          "title": "Check-in Cầu Vàng",
          "description": "Tham quan và chụp ảnh",
          "locationName": "Cầu Vàng",
          "startTime": "09:00:00",
          "endTime": "10:30:00",
          "travelMinutesEstimated": 30
        }
      ]
    }
  ],
  "checklistItems": [
    {
      "itemName": "Áo khoác mỏng",
      "itemGroup": "packing",
      "isRequired": true
    }
  ]
}
```

### `PUT /admin/tours/{id}`

- Permission: `tour.update`
- Body: giống `POST /admin/tours`

### `DELETE /admin/tours/{id}`

- Permission: `tour.delete`

### `GET /admin/tours/{tourId}/schedules`

- Permission: `schedule.view`
- Trả toàn bộ schedule chưa bị soft-delete của tour, không lọc theo status

### `GET /admin/tours/{tourId}/schedules/{scheduleId}`

- Permission: `schedule.view`
- `scheduleId` phải thuộc đúng `tourId`

### `POST /admin/tours/{tourId}/schedules`

- Permission: `schedule.create`

**Rules from current code**

- `departureAt`, `returnAt`, `capacityTotal`, `adultPrice` là bắt buộc
- `capacityTotal >= 1`
- `minGuestsToOperate >= 1` nếu có truyền; nếu bỏ trống backend mặc định `1`
- Các giá đều phải `>= 0`
- `returnAt` phải sau `departureAt`
- `bookingCloseAt` phải sau `bookingOpenAt` nếu cả hai cùng có
- `bookingCloseAt` không được sau `departureAt`
- `meetingAt` không được sau `departureAt`
- `minGuestsToOperate` không được lớn hơn `capacityTotal`
- `pickupPoints[].pickupAt` không được sau `departureAt`
- `guideAssignments[].guideId` nếu có truyền thì phải `> 0`
- `guideAssignments[].guideId` không được trùng nhau trong cùng schedule payload
- Guide được assign phải tồn tại và đang ở trạng thái `active`
- Nếu `status` bỏ trống, backend mặc định `draft`
- Nếu `scheduleCode` bỏ trống, backend tự sinh theo dạng `SCH<timestamp>`
- Backend hiện đồng bộ child list bằng cách replace toàn bộ `pickupPoints` và `guideAssignments`

**Request**

```json
{
  "scheduleCode": "SCH-SGN-20260510",
  "departureAt": "2026-05-10T08:00:00",
  "returnAt": "2026-05-12T18:00:00",
  "bookingOpenAt": "2026-04-01T00:00:00",
  "bookingCloseAt": "2026-05-09T23:00:00",
  "meetingAt": "2026-05-10T07:30:00",
  "meetingPointName": "Chợ Bến Thành",
  "meetingAddress": "Quận 1, TP.HCM",
  "meetingLatitude": 10.7721,
  "meetingLongitude": 106.6983,
  "capacityTotal": 20,
  "minGuestsToOperate": 5,
  "adultPrice": 1000000,
  "childPrice": 600000,
  "infantPrice": 0,
  "seniorPrice": 800000,
  "singleRoomSurcharge": 300000,
  "transportDetail": "Xe giường nằm 29 chỗ",
  "note": "Có mặt trước 30 phút",
  "status": "open",
  "pickupPoints": [
    {
      "pointName": "Chợ Bến Thành",
      "address": "Quận 1, TP.HCM",
      "latitude": 10.7721,
      "longitude": 106.6983,
      "pickupAt": "2026-05-10T07:00:00",
      "sortOrder": 1
    }
  ],
  "guideAssignments": [
    {
      "guideId": 99,
      "guideRole": "lead"
    }
  ]
}
```

### `PUT /admin/tours/{tourId}/schedules/{scheduleId}`

- Permission: `schedule.update`
- Body: giống `POST /admin/tours/{tourId}/schedules`
- Nếu `status` được truyền trong body, backend vẫn validate transition như luồng update status riêng

### `PATCH /admin/tours/{tourId}/schedules/{scheduleId}/status`

- Permission: `schedule.close`

**Rules from current code**

- Body chỉ gồm `status`
- Không cho đưa schedule đã có `bookedSeats > 0` quay lại `draft`
- Không cho reopen schedule quá ngày khởi hành về `open` hoặc `full`
- Nếu caller set `status = open` nhưng `bookedSeats >= capacityTotal`, backend tự chuyển thành `full`

**Request**

```json
{
  "status": "closed"
}
```

### TourResponse shape

```json
{
  "id": 1,
  "code": "TOUR-BNH-2026",
  "name": "Tour Bà Nà Hills 2 ngày 1 đêm",
  "slug": "tour-ba-na-hills-2n1d",
  "destinationId": 1,
  "cancellationPolicyId": 1,
  "basePrice": 1500000,
  "currency": "VND",
  "durationDays": 2,
  "durationNights": 1,
  "shortDescription": "Tour ngắn ngày cho gia đình",
  "description": "Lịch trình bao gồm cáp treo, Cầu Vàng và Fantasy Park",
  "transportType": "BUS",
  "tripMode": "GROUP",
  "highlights": "Cầu Vàng, Làng Pháp, Fantasy Park",
  "inclusions": "Xe đưa đón, cáp treo, vé vào cửa",
  "exclusions": "Chi phí cá nhân",
  "notes": "Mang giày thể thao",
  "isFeatured": true,
  "status": "active",
  "tags": [
    {
      "id": 1,
      "code": "GIAI_TRI",
      "name": "Giải trí",
      "tagGroup": "phong_cach",
      "description": "Tour vui chơi, hoạt động sôi động"
    },
    {
      "id": 4,
      "code": "GIA_DINH",
      "name": "Gia đình",
      "tagGroup": "doi_tuong",
      "description": "Tour phù hợp gia đình và trẻ em"
    }
  ],
  "media": [
    {
      "id": 101,
      "mediaType": "image",
      "mediaUrl": "https://cdn.example.com/tours/ba-na-cover.jpg",
      "altText": "Bà Nà cover",
      "sortOrder": 0,
      "isActive": true
    }
  ],
  "seasonality": [
    {
      "id": 151,
      "seasonName": "Mùa hè",
      "monthFrom": 5,
      "monthTo": 8,
      "recommendationScore": 9.5,
      "notes": "Thời tiết đẹp, phù hợp gia đình"
    }
  ],
  "itineraryDays": [
    {
      "id": 201,
      "dayNumber": 1,
      "title": "Khởi hành đi Bà Nà",
      "description": "Tập trung và di chuyển lên khu du lịch",
      "overnightDestinationId": null,
      "items": [
        {
          "id": 301,
          "sequenceNo": 1,
          "itemType": "visit",
          "title": "Check-in Cầu Vàng",
          "description": "Tham quan và chụp ảnh",
          "destinationId": null,
          "locationName": "Cầu Vàng",
          "address": null,
          "latitude": null,
          "longitude": null,
          "googleMapUrl": null,
          "startTime": "09:00:00",
          "endTime": "10:30:00",
          "travelMinutesEstimated": 30
        }
      ]
    }
  ],
  "checklistItems": [
    {
      "id": 401,
      "itemName": "Áo khoác mỏng",
      "itemGroup": "packing",
      "isRequired": true
    }
  ],
  "cancellationPolicy": {
    "id": 1,
    "name": "CHINH_SACH_MAC_DINH",
    "description": "Chính sách hoàn hủy mặc định của TravelViet",
    "voucherBonusPercent": 10,
    "isDefault": true,
    "isActive": true,
    "rules": [
      {
        "id": 1,
        "minHoursBefore": 168,
        "maxHoursBefore": null,
        "refundPercent": 80,
        "voucherPercent": 90,
        "feePercent": 20,
        "allowReschedule": true,
        "notes": "Hủy trước 7 ngày"
      }
    ]
  ]
}
```

### TourScheduleResponse shape

```json
{
  "id": 66,
  "scheduleCode": "SCH-SGN-20260510",
  "tourId": 15,
  "departureAt": "2026-05-10T08:00:00",
  "returnAt": "2026-05-12T18:00:00",
  "bookingOpenAt": "2026-04-01T00:00:00",
  "bookingCloseAt": "2026-05-09T23:00:00",
  "meetingAt": "2026-05-10T07:30:00",
  "meetingPointName": "Chợ Bến Thành",
  "meetingAddress": "Quận 1, TP.HCM",
  "meetingLatitude": 10.7721,
  "meetingLongitude": 106.6983,
  "capacityTotal": 20,
  "bookedSeats": 5,
  "remainingSeats": 15,
  "minGuestsToOperate": 5,
  "adultPrice": 1000000,
  "childPrice": 600000,
  "infantPrice": 0,
  "seniorPrice": 800000,
  "singleRoomSurcharge": 300000,
  "transportDetail": "Xe giường nằm 29 chỗ",
  "note": "Có mặt trước 30 phút",
  "status": "open",
  "pickupPoints": [
    {
      "id": 10,
      "pointName": "Chợ Bến Thành",
      "address": "Quận 1, TP.HCM",
      "latitude": 10.7721,
      "longitude": 106.6983,
      "pickupAt": "2026-05-10T07:00:00",
      "sortOrder": 1
    }
  ],
  "guideAssignments": [
    {
      "id": 20,
      "guideId": 99,
      "guideCode": "GD099",
      "guideFullName": "Le Van Guide",
      "guidePhone": "0909000000",
      "guideEmail": "guide99@example.com",
      "guideStatus": "active",
      "isLocalGuide": true,
      "guideRole": "lead",
      "assignedAt": "2026-04-01T09:00:00"
    }
  ]
}
```

---

## 8. Bookings

### `POST /bookings/quote`

- Permission: `booking.create`

**Luu y nghiep vu tu code**

- endpoint nay chi tinh gia, khong tao booking
- `tourId`, `scheduleId` la bat buoc
- `adults` toi thieu `1`
- backend van ap rule schedule giong `POST /bookings`:
  - `scheduleId` phai ton tai
  - schedule phai thuoc dung `tourId`
  - schedule phai o trang thai `open`
  - neu co `bookingOpenAt` / `bookingCloseAt` thi phai nam trong cua so dat cho
  - kiem tra suc chua theo `adults + children + seniors`
- `subtotalAmount` duoc tinh theo bang gia `tour_schedule`
- neu co `comboId`, backend se:
  - load `combo_packages`
  - yeu cau combo dang `isActive = true`
  - tinh `addonAmount = max(basePrice - discountAmount, 0)`
  - cong `addonAmount` vao `finalAmount`
  - tra ve `appliedCombo` de booking.create dung lai lam snapshot
- neu co `voucherCode`, backend se:
  - normalize uppercase truoc khi lookup
  - yeu cau voucher da duoc claim boi current user
  - check voucher con active, con trong khung hieu luc
  - check `usageLimitTotal` va `usageLimitPerUser`
  - check `minOrderValue`
  - check `applicableScope` theo `all|tour|destination`
  - check `applicableMemberLevel` neu voucher co rang buoc member level
- o pham vi hien tai, chi `percentage` va `fixed_amount` voucher moi duoc tru vao payable amount
- voucher `gift` va `cashback` hien se bi reject trong quote pricing
- `POST /bookings` dung chung pricing engine nay de persist pricing breakdown va `voucherId`

**Request**

```json
{
  "tourId": 1,
  "scheduleId": 5,
  "adults": 2,
  "children": 1,
  "infants": 0,
  "seniors": 0,
  "comboId": 6,
  "voucherCode": "SPRING10"
}
```

**Response**

```json
{
  "success": true,
  "message": "Booking quote calculated",
  "data": {
    "tourId": 1,
    "scheduleId": 5,
    "adults": 2,
    "children": 1,
    "infants": 0,
    "seniors": 0,
    "seatCount": 3,
    "travellerCount": 3,
    "subtotalAmount": 2850000,
    "discountAmount": 200000,
    "voucherDiscountAmount": 150000,
    "loyaltyDiscountAmount": 0,
    "addonAmount": 50000,
    "taxAmount": 0,
    "finalAmount": 2750000,
    "currency": "VND",
    "appliedCombo": {
      "comboId": 6,
      "comboCode": "COMBO-001",
      "comboName": "Adventure Combo",
      "unitPrice": 100000,
      "discountAmount": 50000,
      "finalPrice": 50000
    },
    "appliedVoucher": {
      "claimId": 9,
      "voucherId": 5,
      "voucherCode": "SPRING10",
      "voucherName": "Spring 10",
      "discountType": "percentage",
      "discountValue": 10,
      "maxDiscountAmount": 150000
    }
  }
}
```

### `POST /bookings`

- Permission: `booking.create`

**Lưu ý nghiệp vụ từ code**

- `userId`, `tourId`, `scheduleId`, `contactName`, `contactPhone` là bắt buộc
- `adults` tối thiểu `1`
- Nếu user thường gọi API, backend ưu tiên user trong token
- `scheduleId` phải tồn tại, phải thuộc đúng `tourId`, và schedule phải ở trạng thái `open`
- Nếu `bookingOpenAt` hoặc `bookingCloseAt` có dữ liệu, backend sẽ áp dụng cửa sổ đặt chỗ theo thời điểm hiện tại
- Backend kiểm tra sức chứa schedule theo số ghế thực chiếm: `adults + children + seniors`
- Backend gọi chung `BookingPricingService` để tính:
  - `subtotalAmount`
  - `discountAmount`
  - `voucherDiscountAmount`
  - `addonAmount`
  - `finalAmount`
  - `voucherId`
- nếu có `comboId`, booking sẽ:
  - persist `comboId` lên `bookings`
  - ghi snapshot vào `booking_combo_items`
  - dùng giá combo tại thời điểm đặt, không phụ thuộc giá live sau đó
- Nếu có `voucherCode`, backend áp cùng rule như `POST /bookings/quote`:
  - voucher phải được current user claim trước
  - còn hiệu lực, còn quota, đạt `minOrderValue`
  - khớp `applicableScope` và `applicableMemberLevel`
  - hiện chỉ support `percentage` và `fixed_amount`
- trong phase hiện tại, voucher chỉ discount phần tour subtotal; combo được cộng như add-on riêng
- `passengers[].dateOfBirth` được parse theo định dạng `yyyy-MM-dd` và map xuống entity
- `passengers[].passengerType` hợp lệ: `adult`, `child`, `infant`, `senior`
- `passengers[].gender` hợp lệ: `male`, `female`, `other`, `unknown`; nếu để trống backend sẽ dùng `unknown`

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
  "comboId": 6,
  "voucherCode": "SPRING10",
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
    "subtotalAmount": 3000000,
    "discountAmount": 200000,
    "voucherDiscountAmount": 150000,
    "addonAmount": 50000,
    "finalAmount": 2850000,
    "voucherId": 5,
    "comboId": 6
  }
}
```

### `GET /bookings/{id}`

- Permission: `booking.view`

### `GET /bookings/{id}/status-history`

- Permission: `booking.view`
- Trả về lịch sử chuyển trạng thái của booking theo thứ tự thời gian tăng dần

### `PATCH /bookings/{id}/cancel`

- Permission: `booking.cancel`

**Lưu ý nghiệp vụ từ code**

- Chỉ cho phép khi booking đang ở `pending_payment` hoặc `confirmed`
- Nếu booking đã thanh toán, backend chuyển sang `cancel_requested`
- Nếu booking chưa thanh toán, backend chuyển thẳng sang `cancelled`
- Backend ghi thêm `booking_status_history`

**Request**

```json
{
  "reason": "Khách yêu cầu hủy booking"
}
```

### `PATCH /bookings/{id}/check-in`

- Permission: `booking.checkin`

**Lưu ý nghiệp vụ từ code**

- Chỉ booking `confirmed` và `paymentStatus = paid` mới được check-in
- Backend chuyển status sang `checked_in`
- Backend ghi thêm `booking_status_history`

### `PATCH /bookings/{id}/complete`

- Permission: `booking.update`

**Lưu ý nghiệp vụ từ code**

- Chỉ booking `checked_in` mới được complete
- Backend chuyển status sang `completed`
- Backend ghi thêm `booking_status_history`

---

## 9. Payments

### `POST /payments`

- Permission: `payment.create`

**Lưu ý nghiệp vụ từ code**

- DTO bắt buộc: `bookingId`, `paymentMethod`, `amount`
- `amount` phải lớn hơn `0`
- Booking phải tồn tại và người gọi phải có quyền truy cập booking đó
- Booking chỉ được thanh toán khi đang ở trạng thái `pending_payment` hoặc `confirmed`
- Booking không được ở trạng thái thanh toán `paid` hoặc `refunded`
- `amount` phải khớp tuyệt đối với `booking.finalAmount`
- Backend tự set:
  - `currency = "VND"`
  - `status = "paid"`
  - `paidAt = now()`

**Additional notes**

- After creating the payment, booking `status` is updated to `confirmed`
- After creating the payment, booking `paymentStatus` is updated to `paid`
- If the booking has `voucherId`, the service increments both:
  - `vouchers.usedCount`
  - `voucher_user_claims.usedCount` for `(voucherId, booking.userId)`
- The service also rejects duplicate successful payments for the same booking
- `paymentMethod` is currently accepted as a plain `string`; the DTO layer does not enforce an enum yet

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
- `requestedAmount` phải lớn hơn `0`
- Chỉ booking đã thanh toán (`paymentStatus = paid`) mới được tạo refund request
- `requestedAmount` không được vượt `booking.finalAmount`
- Backend chặn tạo refund request mới nếu booking đã có refund đang active
- `requestedBy` nếu không phải backoffice sẽ bị override bằng user đang login
- Backend gọi stored procedure `sp_get_refund_quote`
- `requestedAmount` không được vượt `refundable_amount` trả về từ quote
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
- Chỉ refund ở trạng thái `requested` mới được approve
- `approvedAmount` phải lớn hơn `0`
- `approvedAmount` không được vượt `requestedAmount`
- `approvedAmount` không được vượt `quotedAmount`
- Hệ thống tạo thêm payment record:
  - `paymentMethod = "refund"`
  - `status = "refunded"`
- Booking status được update thành `refunded`
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
- Validation:
  - `page >= 0`
  - `1 <= size <= 100`

### `GET /reviews/me`

- Permission: `review.view`
- Query params: `page`, `size`
- Validation:
  - `page >= 0`
  - `1 <= size <= 100`

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

| Endpoint                                                                               | Access                                        |
| -------------------------------------------------------------------------------------- | --------------------------------------------- |
| `GET /system/health`                                                                   | `PUBLIC`                                      |
| `POST /auth/register`                                                                  | `PUBLIC`                                      |
| `POST /auth/login`                                                                     | `PUBLIC`                                      |
| `POST /auth/refresh`                                                                   | `PUBLIC`                                      |
| `GET /users/me`                                                                        | `AUTHENTICATED`                               |
| `PUT /users/me`                                                                        | `AUTHENTICATED`                               |
| `GET /users/me/preferences`                                                            | `AUTHENTICATED`                               |
| `PUT /users/me/preferences`                                                            | `AUTHENTICATED`                               |
| `GET /users/me/devices`                                                                | `AUTHENTICATED`                               |
| `POST /users/me/devices`                                                               | `AUTHENTICATED`                               |
| `DELETE /users/me/devices/{id}`                                                        | `AUTHENTICATED`                               |
| `GET /users/me/addresses`                                                              | `AUTHENTICATED`                               |
| `POST /users/me/addresses`                                                             | `AUTHENTICATED`                               |
| `PUT /users/me/addresses/{id}`                                                         | `AUTHENTICATED`                               |
| `PATCH /users/me/addresses/{id}/default`                                               | `AUTHENTICATED`                               |
| `DELETE /users/me/addresses/{id}`                                                      | `AUTHENTICATED`                               |
| `GET /users/me/wishlist/tours`                                                         | `AUTHENTICATED`                               |
| `POST /users/me/wishlist/tours/{tourId}`                                               | `AUTHENTICATED`                               |
| `DELETE /users/me/wishlist/tours/{tourId}`                                             | `AUTHENTICATED`                               |
| `GET /users/me/tour-views`                                                             | `AUTHENTICATED`                               |
| `POST /notifications`                                                                  | `user.update`                                 |
| `GET /users/me/notifications`                                                          | `AUTHENTICATED`                               |
| `PATCH /users/me/notifications/{id}/read`                                              | `AUTHENTICATED`                               |
| `PATCH /users/me/notifications/read-all`                                               | `AUTHENTICATED`                               |
| `POST /users/me/recommendations/tours`                                                 | `AUTHENTICATED`                               |
| `GET /users/me/recommendations/logs`                                                   | `AUTHENTICATED`                               |
| `POST /users/me/support/sessions`                                                      | `support.reply`                               |
| `GET /users/me/support/sessions`                                                       | `support.view`                                |
| `GET /users/me/support/sessions/{id}`                                                  | `support.view`                                |
| `POST /users/me/support/sessions/{id}/messages`                                        | `support.reply`                               |
| `GET /schedules/{scheduleId}/chat-room`                                                | `AUTHENTICATED`                               |
| `GET /schedules/{scheduleId}/chat-room/messages`                                       | `AUTHENTICATED`                               |
| `POST /schedules/{scheduleId}/chat-room/messages`                                      | `AUTHENTICATED`                               |
| `GET /users/me/passport`                                                               | `AUTHENTICATED`                               |
| `GET /users/me/checkins`                                                               | `AUTHENTICATED`                               |
| `GET /admin/schedules/{scheduleId}/chat-room`                                          | `schedule.view`                               |
| `PUT /admin/schedules/{scheduleId}/chat-room`                                          | `schedule.update`                             |
| `GET /admin/schedules/{scheduleId}/chat-room/messages`                                 | `schedule.view`                               |
| `POST /admin/schedules/{scheduleId}/chat-room/messages`                                | `schedule.update`                             |
| `GET /support/sessions`                                                                | `support.view`                                |
| `GET /support/sessions/{id}`                                                           | `support.view`                                |
| `PATCH /support/sessions/{id}/assign`                                                  | `support.assign`                              |
| `PATCH /support/sessions/{id}/status`                                                  | `support.assign`                              |
| `POST /support/sessions/{id}/messages`                                                 | `support.reply`                               |
| `GET /destinations/{destinationUuid}/weather/forecasts`                                | `PUBLIC`                                      |
| `GET /destinations/{destinationUuid}/weather/alerts`                                   | `PUBLIC`                                      |
| `GET /destinations/{destinationUuid}/weather/crowd-predictions`                        | `PUBLIC`                                      |
| `GET /route-estimates`                                                                 | `PUBLIC`                                      |
| `GET /admin/destinations/{destinationUuid}/weather/forecasts`                          | `destination.view`                            |
| `PUT /admin/destinations/{destinationUuid}/weather/forecasts/{forecastDate}`           | `destination.update`                          |
| `GET /admin/destinations/{destinationUuid}/weather/alerts`                             | `destination.view`                            |
| `POST /admin/destinations/{destinationUuid}/weather/alerts`                            | `destination.update`                          |
| `PUT /admin/destinations/{destinationUuid}/weather/alerts/{alertId}`                   | `destination.update`                          |
| `PATCH /admin/destinations/{destinationUuid}/weather/alerts/{alertId}/status`          | `destination.update`                          |
| `GET /admin/destinations/{destinationUuid}/weather/crowd-predictions`                  | `destination.view`                            |
| `PUT /admin/destinations/{destinationUuid}/weather/crowd-predictions/{predictionDate}` | `destination.update`                          |
| `GET /admin/route-estimates`                                                           | `destination.view`                            |
| `POST /admin/route-estimates`                                                          | `destination.update`                          |
| `GET /roles`                                                                           | `user.view`                                   |
| `GET /badges`                                                                          | `user.view`                                   |
| `GET /badges/{id}`                                                                     | `user.view`                                   |
| `POST /badges`                                                                         | `user.update`                                 |
| `PUT /badges/{id}`                                                                     | `user.update`                                 |
| `PATCH /badges/{id}/status`                                                            | `user.update`                                 |
| `POST /badges/{badgeId}/grant/users/{userId}`                                          | `user.update`                                 |
| `POST /users/{userId}/checkins`                                                        | `booking.checkin`                             |
| `GET /roles/{id}`                                                                      | `user.view`                                   |
| `GET /permissions`                                                                     | `user.view`                                   |
| `POST /roles`                                                                          | `role.assign`                                 |
| `PUT /roles/{id}`                                                                      | `role.assign`                                 |
| `PATCH /roles/{id}/permissions`                                                        | `role.assign`                                 |
| `GET /audit-logs`                                                                      | `audit.view`                                  |
| `GET /promotion-campaigns`                                                             | `voucher.view`                                |
| `GET /promotion-campaigns/{id}`                                                        | `voucher.view`                                |
| `POST /promotion-campaigns`                                                            | `voucher.create`                              |
| `PUT /promotion-campaigns/{id}`                                                        | `voucher.update`                              |
| `PATCH /promotion-campaigns/{id}/status`                                               | `voucher.delete`                              |
| `GET /vouchers`                                                                        | `voucher.view`                                |
| `GET /vouchers/{id}`                                                                   | `voucher.view`                                |
| `POST /vouchers`                                                                       | `voucher.create`                              |
| `PUT /vouchers/{id}`                                                                   | `voucher.update`                              |
| `PATCH /vouchers/{id}/status`                                                          | `voucher.delete`                              |
| `GET /users/me/vouchers`                                                               | `isAuthenticated()`                           |
| `POST /vouchers/claim`                                                                 | `isAuthenticated()`                           |
| `GET /products`                                                                        | `voucher.view`                                |
| `GET /products/{id}`                                                                   | `voucher.view`                                |
| `POST /products`                                                                       | `voucher.create`                              |
| `PUT /products/{id}`                                                                   | `voucher.update`                              |
| `PATCH /products/{id}/status`                                                          | `voucher.delete`                              |
| `GET /combo-packages`                                                                  | `voucher.view`                                |
| `GET /combo-packages/{id}`                                                             | `voucher.view`                                |
| `POST /combo-packages`                                                                 | `voucher.create`                              |
| `PUT /combo-packages/{id}`                                                             | `voucher.update`                              |
| `PATCH /combo-packages/{id}/status`                                                    | `voucher.delete`                              |
| `POST /users`                                                                          | `user.create`                                 |
| `GET /users`                                                                           | `user.view`                                   |
| `GET /users/{id}`                                                                      | `user.view`                                   |
| `PUT /users/{id}`                                                                      | `user.update`                                 |
| `PATCH /users/{id}/deactivate`                                                         | `user.block` or `user.delete`                 |
| `GET /destinations`                                                                    | `PUBLIC`                                      |
| `GET /destinations/{uuid}`                                                             | `PUBLIC`                                      |
| `POST /destinations/propose`                                                           | `destination.propose` or `destination.create` |
| `GET /admin/destinations`                                                              | `destination.view`                            |
| `GET /admin/destinations/{uuid}`                                                       | `destination.view`                            |
| `POST /admin/destinations`                                                             | `destination.create`                          |
| `PUT /admin/destinations/{uuid}`                                                       | `destination.update`                          |
| `DELETE /admin/destinations/{uuid}`                                                    | `destination.delete`                          |
| `PATCH /admin/destinations/{uuid}/approve`                                             | `destination.review` or `destination.publish` |
| `PATCH /admin/destinations/{uuid}/reject`                                              | `destination.review` or `destination.publish` |
| `GET /admin/destinations/{destinationUuid}/weather/forecasts`                          | `destination.view`                            |
| `PUT /admin/destinations/{destinationUuid}/weather/forecasts/{forecastDate}`           | `destination.update`                          |
| `GET /admin/destinations/{destinationUuid}/weather/alerts`                             | `destination.view`                            |
| `POST /admin/destinations/{destinationUuid}/weather/alerts`                            | `destination.update`                          |
| `PUT /admin/destinations/{destinationUuid}/weather/alerts/{alertId}`                   | `destination.update`                          |
| `PATCH /admin/destinations/{destinationUuid}/weather/alerts/{alertId}/status`          | `destination.update`                          |
| `POST /destinations/{uuid}/follow`                                                     | `AUTHENTICATED`                               |
| `DELETE /destinations/{uuid}/follow`                                                   | `AUTHENTICATED`                               |
| `PUT /destinations/{uuid}/follow/settings`                                             | `AUTHENTICATED`                               |
| `GET /destinations/me/follows`                                                         | `AUTHENTICATED`                               |
| `GET /tours`                                                                           | `PUBLIC`                                      |
| `GET /tours/{id}`                                                                      | `PUBLIC`                                      |
| `GET /tours/{id}/schedules`                                                            | `PUBLIC`                                      |
| `GET /tours/{tourId}/schedules/{scheduleId}`                                           | `PUBLIC`                                      |
| `POST /admin/tours`                                                                    | `tour.create`                                 |
| `PUT /admin/tours/{id}`                                                                | `tour.update`                                 |
| `DELETE /admin/tours/{id}`                                                             | `tour.delete`                                 |
| `GET /admin/tours/{tourId}/schedules`                                                  | `schedule.view`                               |
| `GET /admin/tours/{tourId}/schedules/{scheduleId}`                                     | `schedule.view`                               |
| `POST /admin/tours/{tourId}/schedules`                                                 | `schedule.create`                             |
| `PUT /admin/tours/{tourId}/schedules/{scheduleId}`                                     | `schedule.update`                             |
| `PATCH /admin/tours/{tourId}/schedules/{scheduleId}/status`                            | `schedule.close`                              |
| `POST /bookings/quote`                                                                 | `booking.create`                              |
| `POST /bookings`                                                                       | `booking.create`                              |
| `GET /bookings/{id}`                                                                   | `booking.view`                                |
| `POST /payments`                                                                       | `payment.create`                              |
| `GET /payments/{id}`                                                                   | `payment.view`                                |
| `POST /refunds`                                                                        | `refund.create`                               |
| `GET /refunds/{id}`                                                                    | `refund.view`                                 |
| `PATCH /refunds/{id}/approve`                                                          | `refund.approve` or `refund.process`          |
| `POST /reviews`                                                                        | `review.create`                               |
| `GET /reviews/{id}`                                                                    | `review.view`                                 |
| `GET /reviews/tours/{tourId}`                                                          | `review.view`                                 |
| `GET /reviews/me`                                                                      | `review.view`                                 |
| `POST /reviews/{id}/replies`                                                           | `review.reply`                                |
| `PATCH /reviews/{id}/moderation`                                                       | `review.moderate`                             |

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

- `BookingStatus`: `pending_payment`, `confirmed`, `checked_in`, `completed`, `cancel_requested`, `cancelled`, `refunded`, `expired`
- `BookingPaymentStatus`: `unpaid`, `partial`, `paid`, `failed`, `refunded`, `chargeback`
- `PaymentStatus`: `unpaid`, `partial`, `paid`, `failed`, `refunded`, `chargeback`
- `RefundStatus`: `requested`, `quoted`, `approved`, `rejected`, `processing`, `completed`, `cancelled`
- `passengerType`: `adult`, `child`, `infant`, `senior`
- `paymentMethod`: source hiện nhận `string`; ví dụ `cash`, `bank_transfer`, `credit_card`, `e_wallet`, `qr`, `gateway`
- `reasonType`: ví dụ `CANCEL_BY_USER`, `FORCE_CANCEL`, `DUPLICATE_BOOKING`

### Tours

- `TourStatus`: `draft`, `active`, `inactive`, `archived`
- `TourScheduleStatus`: `draft`, `open`, `closed`, `full`, `departed`, `completed`, `cancelled`

### Notifications

- `NotificationType`: `SYSTEM`, `BOOKING`, `PAYMENT`, `PROMOTION`, `REVIEW`, `DESTINATION`, `REMINDER`
- `NotificationChannel`: `IN_APP`, `EMAIL`, `SMS`, `PUSH`

### Weather

- `WeatherSeverity`: `info`, `watch`, `warning`, `danger`

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
