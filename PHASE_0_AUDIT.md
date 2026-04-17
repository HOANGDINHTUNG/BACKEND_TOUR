# Phase 0 Audit

## Status

- `Phase 0`: done
- `Task 0.1`: done
  - Fixed `tour.create` / `tour.update` to bind `destinationId` correctly
- `Task 0.2`: done
  - Established current-state coverage matrix
  - Audited enum/status usage
  - Audited existing automated tests
- `Task 0.3`: done
  - Standardized lifecycle status fields for booking, payment, refund, tour, and tour schedule
  - Replaced active-flow string literals with type-safe enums in service logic
- `Task 0.4`: done
  - Added service-level baseline tests for `tours`, `bookings`, `payments`, and `reviews`
  - Baseline test execution was later verified after the Maven wrapper bootstrap fix
- `Task 0.5`: done
  - Published a table-to-layer implementation checklist
  - Labeled ERD areas as `implemented`, `mvp`, or `schema-only`
- `Task 0.6`: done
  - Fixed `mvnw.cmd` bootstrap on this Windows environment
  - Executed the new baseline tests for `tours`, `bookings`, `payments`, and `reviews`

## Current Coverage Matrix

| Domain | ERD coverage | Code coverage | Test coverage | Current assessment |
| --- | --- | --- | --- | --- |
| `auth` | Depends on `users`, `roles`, `permissions`, `user_roles`, `role_permissions` | Controller, facade, command/query services, JWT/security classes present | `AuthControllerTest`, `JwtServiceTest`, `SecurityIntegrationTest` | Core auth is present |
| `users` core | `users`, `roles`, `permissions`, `user_roles` | Entity/repository/service/controller present | Controller/service/repository tests present | Core user management is relatively stable |
| `users` extras | `user_preferences`, `user_devices`, `user_addresses` | All three now have entity/repository/service/controller flows inside `users/profile` | `UserProfileServiceTest`, `UserProfileControllerTest`, `Phase2UserAdminIntegrationTest` | User extras phase is active at self-profile scope with integration coverage |
| `audit/admin ops` | `audit_logs` | Entity/repository/query/controller flow present; `AuditTrailRecorder` now records RBAC and admin user write events via shared helper | `AdminAuditLogQueryServiceTest`, `AdminAuditLogControllerTest`, `AdminRbacCommandServiceTest`, `AdminUserServiceTest`, `Phase2UserAdminIntegrationTest` | Audit is active, but producer scope is still partial |
| `destinations` | `destinations`, `destination_media`, `destination_foods`, `destination_specialties`, `destination_activities`, `destination_tips`, `destination_events`, `destination_follows` | Entity/repository/service/controller present | Proposal and admin update integration tests only | Public/admin flows exist but test coverage is partial |
| `tours` core | `tours`, `tour_media`, `tour_seasonality`, `tour_itinerary_days`, `itinerary_items`, `tour_checklist_items`, `tour_schedules`, `tour_schedule_pickup_points`, `tour_schedule_guides` | Entities and repositories exist, create/update/query flows exist | `TourCommandServiceImplTest`, `TourQueryServiceImplTest` | Tour root orchestration, schedule management, and search hardening are active at MVP level |
| `tours` supporting master data | `tags`, `guides`, `cancellation_policies`, `cancellation_policy_rules`, `tour_tags` | Entity/repository/service/query flows present through tour root and schedule management | Tour command/query tests present | Supporting catalog bindings are active, but still exposed indirectly through tour APIs |
| `bookings` | `bookings`, `booking_passengers`, `booking_status_history`, `booking_combo_items` | Entity/repository/service/controller present; quote pricing engine, lifecycle history, voucher pricing, and combo snapshot flow are active | `BookingCommandServiceImplTest`, `BookingPricingServiceTest`, `BookingControllerTest`, `Phase3CommerceIntegrationTest` | Core booking flow is active with commerce integration; `booking_products` remains pending |
| `payments` | `payments`, `refund_requests` | Entity/repository/service/controller present; successful payment now syncs booking + voucher usage counters | `PaymentCommandServiceImplTest`, `RefundServiceImplTest`, `Phase1LifecycleIntegrationTest`, `Phase3CommerceIntegrationTest` | Core payment/refund flow is active and hardened at service level |
| `promotions/vouchers` | `promotion_campaigns`, `vouchers`, `voucher_user_claims` | Entity/repository/service/controller present | Service/controller tests present | Admin core flow, user claim/ownership flow, and booking/payment integration are active |
| `commerce catalog` | `products`, `combo_packages`, `combo_package_items` | Entity/repository/service/controller present in `module/commerce` | Service/controller tests present | Admin product and combo catalog flow is active |
| `engagement` | `wishlist_tours`, `user_tour_views`, `notifications`, `recommendation_logs` | Entity/repository/service/controller are present in `module/engagement` and `module/notifications`; public tour detail now hooks view logging for authenticated users, and self-profile recommendation flow now persists result snapshots | `UserWishlistServiceImplTest`, `UserWishlistControllerTest`, `UserTourViewServiceImplTest`, `UserTourViewControllerTest`, `TourControllerTest`, `AdminNotificationServiceTest`, `UserNotificationServiceTest`, `AdminNotificationControllerTest`, `UserNotificationControllerTest`, `UserRecommendationServiceImplTest`, `UserRecommendationControllerTest` | Wishlist, view-tracking, in-app notification foundation, and recommendation groundwork are active |
| `weather` | `weather_forecasts`, `weather_alerts`, `crowd_predictions`, `route_estimates` | Entity/repository/service/controller present in `module/weather`; public destination weather read, public crowd prediction read, public/admin route estimate flow, and admin weather management are active | `PublicWeatherServiceTest`, `AdminWeatherServiceTest`, `WeatherControllerTest`, `AdminWeatherControllerTest`, `RouteEstimateControllerTest`, `AdminRouteEstimateControllerTest` | Forecast, alert, crowd prediction, and route estimate foundations are active at MVP level |
| `loyalty` | `travel_passports`, `badge_definitions`, `passport_badges`, `passport_visited_destinations`, `user_checkins` | Entity/repository/service/controller present in `module/loyalty`; self-profile passport read, admin badge catalog/grant, manual checkin, and booking check-in sync flow are active | `UserPassportServiceTest`, `AdminBadgeServiceTest`, `UserPassportControllerTest`, `AdminBadgeControllerTest`, `UserCheckinControllerTest`, `AdminUserCheckinControllerTest`, `BookingCommandServiceImplTest` | Loyalty groundwork is active with check-in automation connected to booking lifecycle |
| `schedule chat` | `schedule_chat_rooms`, `schedule_chat_room_members`, `schedule_chat_messages` | Entity/repository/service/controller present in `module/schedulechat`; user/admin room read, admin room upsert, and message flow are active | `ScheduleChatServiceTest`, `UserScheduleChatControllerTest`, `AdminScheduleChatControllerTest` | Schedule chat foundation is active with room bootstrap and member sync from eligible bookings |
| `support` | `support_sessions`, `support_messages` | Entity/repository/service/controller present in `module/support`; user and backoffice messaging foundation is active | `UserSupportServiceTest`, `AdminSupportServiceTest`, `UserSupportControllerTest`, `AdminSupportControllerTest` | Support session assignment, status, and message lifecycle are active at MVP level |
| `reviews` | `reviews`, `review_aspects`, `review_replies` | Entity/repository/service/controller present | No tests found | Core flow exists |
| `reviews` analysis | `review_analysis` | No entity/repository/service/controller found | No tests | Schema exists, feature not implemented |
| `system` | health only | Controller/facade/query present | No dedicated test found | Minimal support endpoint only |

## Tables In ERD But Not Implemented In Backend Flow

### Booking and commerce extensions

- `booking_products`

### Engagement, recommendation, analytics

- `mission_definitions`
- `user_missions`
- `review_analysis`

## Enum and Status Audit

### Already strongly typed

- `users.status` -> `Status`
- `users.gender` -> `Gender`
- `users.member_level` -> `MemberLevel`
- `users.user_category` -> `UserCategory`
- `roles.role_scope` -> `RoleScope`
- `destinations.status` -> `DestinationStatus`
- `destinations.crowd_level_default` -> `CrowdLevel`
- `destination_media.media_type` -> `MediaType`
- `reviews.sentiment` uses `ReviewSentiment` normalization

### Still string-based and should be normalized in later work

- `booking_passengers.gender`
- `booking_passengers.passenger_type`

### Recommendation

- Lifecycle status fields for active flows have been standardized
- Remaining enum normalization can focus on passenger fields and other schema-only modules when they become active

## Automated Test Audit

### Present

- `auth`
- `users`
- `destinations` partial
- `tours`
- `bookings`
- `payments`
- `refunds`
- `reviews`
- `promotions`
- `commerce`
- `engagement`
- `notifications`
- `recommendations`
- `weather`
- `loyalty`
- `support`
- `schedule chat`
- common security/logging

### Missing for active business flows

- `system`

## Definition Of Done For Phase 0

- Have one stable coverage baseline for tables, code layers, and tests
- Mark all schema-first but feature-inactive areas explicitly
- Define which lifecycle fields must be converted from string to enum before Phase 1
- Add a minimal test baseline for every active money or booking-related flow

## Verification Notes

- `mvnw.cmd -v` now works on this machine after the wrapper bootstrap fix
- Executed targeted tests:
  - `TourCommandServiceImplTest`
  - `BookingCommandServiceImplTest`
  - `PaymentCommandServiceImplTest`
  - `ReviewServiceImplTest`
- Runtime warnings still appear with Java 25:
  - Lombok uses deprecated `Unsafe` access
  - Mockito inline mock maker self-attaches dynamically
- These warnings did not block the baseline test run, but they should be cleaned up in a later tooling pass

## Next Recommended Work

- Begin planning `Phase 5`
- Priority target:
  - mission definitions / user missions or deeper loyalty automation
