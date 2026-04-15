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
  - Test execution is still blocked by the current Maven wrapper bootstrap issue in this repo
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
| `users` extras | `user_preferences`, `user_devices`, `user_addresses` | No entity/repository/service/controller found | No tests | Schema exists, feature not implemented |
| `destinations` | `destinations`, `destination_media`, `destination_foods`, `destination_specialties`, `destination_activities`, `destination_tips`, `destination_events`, `destination_follows` | Entity/repository/service/controller present | Proposal and admin update integration tests only | Public/admin flows exist but test coverage is partial |
| `tours` core | `tours`, `tour_media`, `tour_seasonality`, `tour_itinerary_days`, `itinerary_items`, `tour_checklist_items`, `tour_schedules`, `tour_schedule_pickup_points`, `tour_schedule_guides` | Entities and repositories exist, create/update/query flows exist | No tests found | Data model exists, API/service layer is still MVP |
| `tours` missing attachments | `tags`, `guides`, `cancellation_policies`, `cancellation_policy_rules`, `tour_tags` | No concrete module flow found | No tests | Schema exists, feature not implemented |
| `bookings` | `bookings`, `booking_passengers` | Entity/repository/service/controller present | No tests found | Core flow exists but still skeletal |
| `bookings` missing lifecycle | `booking_status_history`, `booking_combo_items` | No entity/repository/service/controller found | No tests | Schema exists, feature not implemented |
| `payments` | `payments`, `refund_requests` | Entity/repository/service/controller present | No tests found | Core payment/refund flow exists but lacks hardening |
| `reviews` | `reviews`, `review_aspects`, `review_replies` | Entity/repository/service/controller present | No tests found | Core flow exists |
| `reviews` analysis | `review_analysis` | No entity/repository/service/controller found | No tests | Schema exists, feature not implemented |
| `system` | health only | Controller/facade/query present | No dedicated test found | Minimal support endpoint only |

## Tables In ERD But Not Implemented In Backend Flow

### User and profile extensions

- `user_preferences`
- `user_devices`
- `user_addresses`

### Tour catalog and policy

- `tags`
- `guides`
- `cancellation_policies`
- `cancellation_policy_rules`
- `tour_tags`

### Booking and commerce extensions

- `promotion_campaigns`
- `vouchers`
- `voucher_user_claims`
- `combo_packages`
- `combo_package_items`
- `products`
- `booking_status_history`
- `booking_combo_items`

### Engagement, recommendation, analytics

- `mission_definitions`
- `user_missions`
- `review_analysis`
- `weather_forecasts`
- `weather_alerts`
- `crowd_predictions`
- `route_estimates`
- `notifications`
- `user_tour_views`
- `wishlist_tours`
- `recommendation_logs`
- `audit_logs`

### Support and real-time communication

- `support_sessions`
- `support_messages`
- `schedule_chat_rooms`
- `schedule_chat_room_members`
- `schedule_chat_messages`

### Loyalty and passport

- `travel_passports`
- `badge_definitions`
- `passport_badges`
- `user_checkins`
- `passport_visited_destinations`

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
- `tours` baseline
- `bookings` baseline
- `payments` baseline
- `reviews` baseline
- common security/logging

### Missing for active business flows

- `refunds`
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

- Start `Phase 1`
- Priority target:
  - complete `bookings` pricing and lifecycle
  - harden `payments/refunds`
  - deepen `tours` beyond MVP data wiring
