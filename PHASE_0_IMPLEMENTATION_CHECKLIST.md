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
| `bookings` | yes | yes | yes | yes | yes | `mvp` | Core create/detail exists; voucher-aware pricing and combo-aware add-on pricing are persisted on create |
| `booking_passengers` | yes | yes | yes | indirect | partial | `mvp` | Saved through booking flow; passenger typing still string-based |
| `booking_status_history` | yes | yes | yes | yes | yes | `mvp` | Lifecycle history is now recorded and exposed; transition coverage can still be expanded |
| `booking_products` | no | no | no | no | no | `schema-only` | ERD contains it, backend does not |
| `booking_combo_items` | yes | yes | yes | indirect | yes | `implemented` | Snapshot is recorded through booking create when `comboId` is applied |

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
| `promotion_campaigns` | yes | yes | yes | yes | yes | `implemented` | Admin list/detail/create/update/status flow is active |
| `vouchers` | yes | yes | yes | yes | yes | `implemented` | Admin list/detail/create/update/status flow is active |
| `voucher_user_claims` | yes | yes | yes | yes | yes | `implemented` | User claim/list-owned-vouchers flow is active and usage counts now sync on successful payment |
| `mission_definitions` | no | no | no | no | no | `schema-only` | Not started |
| `user_missions` | no | no | no | no | no | `schema-only` | Not started |
| `combo_packages` | yes | yes | yes | yes | yes | `implemented` | Admin list/detail/create/update/status flow is active |
| `combo_package_items` | yes | indirect | yes | indirect | yes | `implemented` | Managed inside combo package aggregate with nested item validation |
| `products` | yes | yes | yes | yes | yes | `implemented` | Admin list/detail/create/update/status flow is active |

## Forecast, Support, Engagement, Loyalty

| Table | Entity | Repository | Service/Facade | Controller/API | Tests | Status | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `weather_forecasts` | yes | yes | yes | yes | yes | `implemented` | Public forecast read and admin forecast upsert flow are active in `module/weather` |
| `weather_alerts` | yes | yes | yes | yes | yes | `implemented` | Public current-alert read and admin alert create/update/status flow are active in `module/weather` |
| `crowd_predictions` | yes | yes | yes | yes | yes | `implemented` | Public destination crowd prediction read and admin crowd prediction upsert are active in `module/weather` |
| `route_estimates` | yes | yes | yes | yes | yes | `implemented` | Public/admin route estimate list flow and admin create flow are active in `module/weather` |
| `notifications` | yes | yes | yes | yes | yes | `implemented` | In-app notification foundation is active with admin create and self-profile read/mark-read flows |
| `support_sessions` | yes | yes | yes | yes | yes | `implemented` | User create/list/detail and admin list/detail/assign/status flows are active |
| `support_messages` | yes | yes | yes | yes | yes | `implemented` | Customer/staff message flow is active through support session endpoints |
| `schedule_chat_rooms` | yes | yes | yes | yes | yes | `implemented` | Schedule chat foundation is active with user/admin room read, admin upsert, and message flow in `module/schedulechat` |
| `schedule_chat_room_members` | yes | yes | yes | yes | yes | `implemented` | Member rows are auto-synced from eligible bookings and on-demand joins when user/backoffice accesses chat |
| `schedule_chat_messages` | yes | yes | yes | yes | yes | `implemented` | User/admin schedule chat message history and send flow are active |
| `travel_passports` | yes | yes | yes | yes | yes | `implemented` | Self-profile passport read auto-bootstraps missing passport rows in `module/loyalty` |
| `badge_definitions` | yes | yes | yes | yes | yes | `implemented` | Admin badge catalog create/update/status flow is active in `module/loyalty` |
| `passport_badges` | yes | yes | yes | yes | yes | `implemented` | Admin badge grant flow is active and idempotent |
| `user_checkins` | yes | yes | yes | yes | yes | `implemented` | Manual admin checkin, self-profile history, and booking check-in sync flow are active in `module/loyalty` |
| `passport_visited_destinations` | yes | yes | yes | indirect | yes | `implemented` | Self-profile passport response reads visited-destination snapshots when present |
| `user_tour_views` | yes | yes | yes | yes | yes | `implemented` | Current user tour detail views are recorded with cooldown; self-profile history API is active |
| `wishlist_tours` | yes | yes | yes | yes | yes | `implemented` | Self-profile wishlist list/add/remove flow is active in `module/engagement` |
| `recommendation_logs` | yes | yes | yes | yes | yes | `implemented` | Self-profile recommendation generate/history flow is active and persists result snapshots to `recommendation_logs` |
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
| Promotion/voucher/combo/product | `mvp` | Promotion campaigns, vouchers, voucher claims, booking pricing, admin combo/product catalog, and booking combo snapshot are active; booking_products is still pending |
| Forecast/support/engagement/loyalty | `implemented` | Weather, crowd prediction, route estimate, loyalty, engagement, support, notification, and schedule chat foundations are all active; remaining gaps are outside this ERD slice |

## How To Use This File In Later Phases

- Before starting a new feature, locate the target table here first
- If the row is `schema-only`, plan the full vertical slice
- If the row is `mvp`, treat the table as a refactor-or-hardening target before adding breadth
- Only treat `implemented` rows as a stable base for downstream features
