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
| `user_preferences` | yes | yes | yes | yes | yes | `implemented` | Self-profile preference get/upsert flow is active |
| `user_devices` | yes | yes | yes | yes | yes | `implemented` | Self-profile device register/list/remove flow is active |
| `user_addresses` | yes | yes | yes | yes | yes | `implemented` | Self-profile address book CRUD/default flow is active |

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
| `tours` | yes | yes | yes | yes | yes | `mvp` | Core create/update/query exists; root tour now orchestrates media, itinerary, checklist, and schedules |
| `tour_media` | yes | yes | yes | indirect | yes | `mvp` | Managed through tour create/update/detail payloads; no standalone endpoint |
| `tour_seasonality` | yes | yes | yes | indirect | yes | `mvp` | Managed through tour root payloads and returned in tour detail |
| `tour_itinerary_days` | yes | yes | yes | indirect | yes | `mvp` | Managed through tour root payloads and returned in tour detail |
| `itinerary_items` | yes | yes | yes | indirect | yes | `mvp` | Managed through itinerary day payloads; no standalone endpoint |
| `tour_checklist_items` | yes | yes | yes | indirect | yes | `mvp` | Managed through tour root payloads and returned in tour detail |
| `tour_schedules` | yes | yes | yes | yes | yes | `mvp` | Admin/public schedule list-detail-create-update-status flow exists; broader lifecycle automation still pending |
| `tour_schedule_pickup_points` | yes | yes | yes | indirect | yes | `mvp` | Managed through schedule create/update payloads; no standalone endpoint |
| `tour_schedule_guides` | yes | yes | yes | indirect | yes | `mvp` | Managed through schedule payloads; guide ids are now validated against active guide master data |
| `tags` | yes | yes | yes | indirect | yes | `mvp` | Active master data is resolved through tour root tagIds and returned in tour detail |
| `guides` | yes | yes | yes | indirect | yes | `mvp` | Guide master data now backs schedule assignment validation and response enrichment |
| `cancellation_policies` | yes | yes | yes | indirect | yes | `mvp` | Bound through tour root; default active policy fallback is enforced |
| `cancellation_policy_rules` | yes | yes | yes | indirect | yes | `mvp` | Returned in tour detail and validated before binding a policy |
| `tour_tags` | yes | yes | yes | indirect | yes | `mvp` | Managed through tour root tagIds; no standalone endpoint |

## Booking

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `bookings` | yes | yes | yes | yes | yes | `mvp` | Core create/detail exists, pricing/lifecycle still incomplete |
| `booking_passengers` | yes | yes | yes | indirect | partial | `mvp` | Saved through booking flow; passenger typing still string-based |
| `booking_status_history` | yes | yes | yes | yes | yes | `mvp` | Lifecycle history is now recorded and exposed; transition coverage can still be expanded |
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
| `audit_logs` | yes | yes | yes | yes | yes | `implemented` | Query API and RBAC write audit producer are active |

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
