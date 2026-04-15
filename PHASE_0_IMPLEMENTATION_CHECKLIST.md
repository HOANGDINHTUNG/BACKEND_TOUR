# Phase 0 Implementation Checklist

## Legend

- `implemented`: table already has working backend flow and at least one meaningful automated test path
- `mvp`: table is mapped in code and/or exposed through API, but flow is still partial, thin, or under-tested
- `schema-only`: table exists in `ERD.sql` but has no real backend implementation yet

## Users And Access

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `users` | yes | yes | yes | yes | yes | `implemented` | Core user profile and admin user flows exist |
| `roles` | yes | yes | yes | indirect | yes | `implemented` | Used by auth and user management |
| `permissions` | yes | no direct repo | yes | indirect | yes | `implemented` | Loaded through role mapping in security flow |
| `role_permissions` | join mapping only | no direct repo | yes | indirect | yes | `implemented` | Managed via JPA many-to-many join |
| `user_roles` | yes | no direct repo | yes | indirect | yes | `implemented` | Used for primary/all roles on user |
| `user_preferences` | no | no | no | no | no | `schema-only` | User preference feature not started |
| `user_devices` | no | no | no | no | no | `schema-only` | Device/push token feature not started |
| `user_addresses` | no | no | no | no | no | `schema-only` | Address book feature not started |

## Destinations

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `destinations` | yes | yes | yes | yes | yes | `implemented` | Public search/detail and admin/proposal flows exist |
| `destination_media` | yes | implicit via destination | yes | yes | partial | `mvp` | Managed through destination aggregate |
| `destination_foods` | yes | implicit via destination | yes | yes | partial | `mvp` | Managed through destination aggregate |
| `destination_specialties` | yes | implicit via destination | yes | yes | partial | `mvp` | Managed through destination aggregate |
| `destination_activities` | yes | implicit via destination | yes | yes | partial | `mvp` | Managed through destination aggregate |
| `destination_tips` | yes | implicit via destination | yes | yes | partial | `mvp` | Managed through destination aggregate |
| `destination_events` | yes | yes | yes | yes | partial | `mvp` | Repository exists, flow is destination-centric |
| `destination_follows` | yes | yes | yes | yes | no | `mvp` | API exists, test coverage still missing |

## Tours

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `tours` | yes | yes | yes | yes | yes | `mvp` | Core create/update/query exists; still thin versus ERD richness |
| `tour_media` | yes | yes | partial | no direct endpoint | no | `mvp` | Mapped but not fully exposed as dedicated flow |
| `tour_seasonality` | yes | yes | partial | no direct endpoint | no | `mvp` | Entity/repository exist, orchestration incomplete |
| `tour_itinerary_days` | yes | yes | partial | no direct endpoint | no | `mvp` | Data model exists, CRUD flow incomplete |
| `itinerary_items` | yes | yes | partial | no direct endpoint | no | `mvp` | Data model exists, CRUD flow incomplete |
| `tour_checklist_items` | yes | yes | partial | no direct endpoint | no | `mvp` | Data model exists, orchestration incomplete |
| `tour_schedules` | yes | yes | partial | no direct endpoint | no | `mvp` | Lifecycle typed, but no real schedule management flow yet |
| `tour_schedule_pickup_points` | yes | yes | partial | no direct endpoint | no | `mvp` | Data model only for now |
| `tour_schedule_guides` | yes | yes | partial | no direct endpoint | no | `mvp` | Depends on missing guide module |
| `tags` | no | no | no | no | no | `schema-only` | Not started |
| `guides` | no | no | no | no | no | `schema-only` | Not started |
| `cancellation_policies` | no | no | no | no | no | `schema-only` | Not started |
| `cancellation_policy_rules` | no | no | no | no | no | `schema-only` | Not started |
| `tour_tags` | no | no | no | no | no | `schema-only` | Not started |

## Booking

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `bookings` | yes | yes | yes | yes | yes | `mvp` | Core create/detail exists, pricing/lifecycle still incomplete |
| `booking_passengers` | yes | yes | yes | indirect | partial | `mvp` | Saved through booking flow; passenger typing still string-based |
| `booking_status_history` | no | no | no | no | no | `schema-only` | Not started |
| `booking_products` | no | no | no | no | no | `schema-only` | ERD contains it, backend does not |
| `booking_combo_items` | no | no | no | no | no | `schema-only` | Not started |

## Payments And Refunds

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `payments` | yes | yes | yes | yes | yes | `mvp` | Create/detail exists; provider integration and robustness still incomplete |
| `refund_requests` | yes | yes | yes | yes | no dedicated test | `mvp` | Flow exists, but no refund-specific automated test yet |

## Reviews

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `reviews` | yes | yes | yes | yes | yes | `mvp` | Core create/detail/reply/moderation exists |
| `review_aspects` | yes | yes | yes | indirect | partial | `mvp` | Managed inside review aggregate |
| `review_replies` | yes | yes | yes | indirect | partial | `mvp` | Managed inside review aggregate |
| `review_analysis` | no | no | no | no | no | `schema-only` | Not started |

## Promotion, Voucher, Mission, Combo, Product

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `promotion_campaigns` | no | no | no | no | no | `schema-only` | Not started |
| `vouchers` | no | no | no | no | no | `schema-only` | Not started |
| `voucher_user_claims` | no | no | no | no | no | `schema-only` | Not started |
| `mission_definitions` | no | no | no | no | no | `schema-only` | Not started |
| `user_missions` | no | no | no | no | no | `schema-only` | Not started |
| `combo_packages` | no | no | no | no | no | `schema-only` | Not started |
| `combo_package_items` | no | no | no | no | no | `schema-only` | Not started |
| `products` | no | no | no | no | no | `schema-only` | Not started |

## Forecast, Support, Engagement, Loyalty

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `weather_forecasts` | no | no | no | no | no | `schema-only` | Not started |
| `weather_alerts` | no | no | no | no | no | `schema-only` | Not started |
| `crowd_predictions` | no | no | no | no | no | `schema-only` | Not started |
| `route_estimates` | no | no | no | no | no | `schema-only` | Not started |
| `notifications` | no | no | no | no | no | `schema-only` | Not started |
| `support_sessions` | no | no | no | no | no | `schema-only` | Not started |
| `support_messages` | no | no | no | no | no | `schema-only` | Not started |
| `schedule_chat_rooms` | no | no | no | no | no | `schema-only` | Not started |
| `schedule_chat_room_members` | no | no | no | no | no | `schema-only` | Not started |
| `schedule_chat_messages` | no | no | no | no | no | `schema-only` | Not started |
| `travel_passports` | no | no | no | no | no | `schema-only` | Not started |
| `badge_definitions` | no | no | no | no | no | `schema-only` | Not started |
| `passport_badges` | no | no | no | no | no | `schema-only` | Not started |
| `user_checkins` | no | no | no | no | no | `schema-only` | Not started |
| `passport_visited_destinations` | no | no | no | no | no | `schema-only` | Not started |
| `user_tour_views` | no | no | no | no | no | `schema-only` | Not started |
| `wishlist_tours` | no | no | no | no | no | `schema-only` | Not started |
| `recommendation_logs` | no | no | no | no | no | `schema-only` | Not started |
| `audit_logs` | no | no | no | no | no | `schema-only` | Not started |

## Phase Label By ERD Area

| ERD area | Delivery label | Reason |
| --- | --- | --- |
| Users and access core | `implemented` | API, service, and tests are already present |
| Destinations | `implemented` for root aggregate, `mvp` for child collections/follows | Main flow works; coverage is still uneven |
| Tours | `mvp` | Core flow exists, but many supporting tables are not operational yet |
| Booking | `mvp` | Core booking exists, lifecycle and pricing still incomplete |
| Payments and refunds | `mvp` | Core flow exists, hardening and dedicated refund test are missing |
| Reviews | `mvp` | Core flow exists, analytics table is still missing |
| Promotion/voucher/combo/product | `schema-only` | No backend implementation yet |
| Forecast/support/engagement/loyalty | `schema-only` | No backend implementation yet |

## How To Use This File In Later Phases

- Before starting a new feature, locate the target table here first
- If the row is `schema-only`, plan the full vertical slice
- If the row is `mvp`, treat the table as a refactor-or-hardening target before adding breadth
- Only treat `implemented` rows as a stable base for downstream features
