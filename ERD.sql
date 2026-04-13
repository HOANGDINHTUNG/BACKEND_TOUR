SET NAMES utf8mb4; SET FOREIGN_KEY_CHECKS = 0; 
CREATE DATABASE IF NOT EXISTS travelviet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 
USE travelviet; 
SET FOREIGN_KEY_CHECKS = 1; 

-- 1. USERS audit_logs/ PREFERENCES / DEVICES -- 

CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_category ENUM('INTERNAL', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    status ENUM('pending', 'active', 'suspended', 'blocked', 'deleted') NOT NULL DEFAULT 'active',
    full_name VARCHAR(150) NOT NULL,
    display_name VARCHAR(120),
    gender ENUM('male', 'female', 'other', 'unknown') NOT NULL DEFAULT 'unknown',
    date_of_birth DATE,
    avatar_url TEXT,
    member_level ENUM('bronze', 'silver', 'gold', 'platinum', 'diamond') NOT NULL DEFAULT 'bronze',
    loyalty_points INT NOT NULL DEFAULT 0,
    total_spent DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    email_verified_at DATETIME NULL,
    phone_verified_at DATETIME NULL,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT chk_user_contact_required CHECK (email IS NOT NULL OR phone IS NOT NULL)
)  ENGINE=INNODB; 

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    role_scope ENUM('SYSTEM', 'BACKOFFICE', 'CUSTOMER') NOT NULL DEFAULT 'SYSTEM',
    hierarchy_level INT NOT NULL DEFAULT 0,
    is_system_role BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB; 

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    module_name VARCHAR(80) NOT NULL,
    action_name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB; 

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by CHAR(36) NULL,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_granted_by
        FOREIGN KEY (granted_by) REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB; 

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    role_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at DATETIME NULL,
    assigned_by CHAR(36) NULL,
    note VARCHAR(255) NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_roles_user_primary (user_id, is_primary),
    KEY idx_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_assigned_by
        FOREIGN KEY (assigned_by) REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB; 

CREATE TABLE IF NOT EXISTS user_preferences ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
	user_id CHAR(36) NOT NULL UNIQUE, 
	budget_level ENUM('low', 'medium', 'high', 'luxury') NULL, 
	preferred_trip_mode ENUM('group', 'private', 'shared') NULL, 
	travel_style ENUM('relax', 'adventure', 'checkin', 'family', 'culture', 'food', 'spiritual', 'mixed') NULL, 
	preferred_departure_city VARCHAR(120), favorite_regions JSON NULL, 
	favorite_tags JSON NULL, favorite_destinations JSON NULL, 
	prefers_low_mobility BOOLEAN NOT NULL DEFAULT FALSE, 
	prefers_family_friendly BOOLEAN NOT NULL DEFAULT FALSE, 
	prefers_student_budget BOOLEAN NOT NULL DEFAULT FALSE, 
	prefers_weather_alert BOOLEAN NOT NULL DEFAULT TRUE, 
	prefers_promotion_alert BOOLEAN NOT NULL DEFAULT TRUE, 
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
	CONSTRAINT fk_user_preferences_user 
	FOREIGN KEY (user_id) 
	REFERENCES users(id) 
	ON DELETE CASCADE 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS user_devices ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
	user_id CHAR(36) NOT NULL, 
	platform VARCHAR(30) NOT NULL, 
	device_name VARCHAR(100), 
	push_token TEXT, 
	app_version VARCHAR(30), 
	is_active BOOLEAN NOT NULL DEFAULT TRUE, 
	last_seen_at DATETIME NULL, 
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
	CONSTRAINT fk_user_devices_user 
	FOREIGN KEY (user_id) 
	REFERENCES users(id) 
	ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    contact_name VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    province VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    address_line TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 

-- 2. DESTINATIONS / CONTENT / FOLLOW -- 


CREATE TABLE IF NOT EXISTS destinations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    country_code CHAR(2) NOT NULL DEFAULT 'VN',
    province VARCHAR(120) NOT NULL,
    district VARCHAR(120),
    region VARCHAR(120),
    address TEXT,
    latitude DECIMAL(10 , 7 ),
    longitude DECIMAL(10 , 7 ),
    short_description TEXT,
    description TEXT,
    best_time_from_month TINYINT NULL,
    best_time_to_month TINYINT NULL,
    crowd_level_default ENUM('low', 'medium', 'high', 'very_high') NOT NULL DEFAULT 'medium',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_best_time_from CHECK (best_time_from_month BETWEEN 1 AND 12
        OR best_time_from_month IS NULL),
    CONSTRAINT chk_best_time_to CHECK (best_time_to_month BETWEEN 1 AND 12
        OR best_time_to_month IS NULL)
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS destination_media ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    destination_id BIGINT NOT NULL, 
    media_type ENUM('image', 'video', 'cover', 'banner') NOT NULL DEFAULT 'image', 
    media_url TEXT NOT NULL, 
    alt_text VARCHAR(255), 
    sort_order INT NOT NULL DEFAULT 0, 
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_destination_media_destination 
    FOREIGN KEY (destination_id) 
    REFERENCES destinations(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS destination_foods ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    destination_id BIGINT NOT NULL, 
    food_name VARCHAR(200) NOT NULL, 
    description TEXT, 
    is_featured BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_destination_foods_destination 
    FOREIGN KEY (destination_id) 
    REFERENCES destinations(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS destination_specialties ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    destination_id BIGINT NOT NULL, 
    specialty_name VARCHAR(200) NOT NULL, 
    description TEXT, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_destination_specialties_destination 
    FOREIGN KEY (destination_id) 
    REFERENCES destinations(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS destination_activities ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    destination_id BIGINT NOT NULL, 
    activity_name VARCHAR(200) NOT NULL, 
    description TEXT, 
    activity_score DECIMAL(5,2) NOT NULL DEFAULT 0, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_destination_activities_destination 
    FOREIGN KEY (destination_id) 
    REFERENCES destinations(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS destination_tips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    destination_id BIGINT NOT NULL,
    tip_title VARCHAR(200) NOT NULL,
    tip_content TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_destination_tips_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS destination_events ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    destination_id BIGINT NOT NULL, 
    event_name VARCHAR(200) NOT NULL, 
    event_type VARCHAR(80), 
    description TEXT, 
    starts_at DATETIME NULL, 
    ends_at DATETIME NULL, 
    notify_all_followers BOOLEAN NOT NULL DEFAULT FALSE, 
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    CONSTRAINT fk_destination_events_destination 
    FOREIGN KEY (destination_id) 
    REFERENCES destinations(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS destination_follows ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    user_id CHAR(36) NOT NULL, 
    destination_id BIGINT NOT NULL, 
    notify_event BOOLEAN NOT NULL DEFAULT TRUE, 
    notify_voucher BOOLEAN NOT NULL DEFAULT TRUE, 
    notify_new_tour BOOLEAN NOT NULL DEFAULT TRUE, 
    notify_best_season BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    UNIQUE KEY uk_destination_follow (user_id, destination_id), 
    CONSTRAINT fk_destination_follows_user 
    FOREIGN KEY (user_id) 
    REFERENCES users(id) 
    ON DELETE CASCADE, 
    CONSTRAINT fk_destination_follows_destination 
    FOREIGN KEY (destination_id) 
    REFERENCES destinations(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


-- 3. TAGS / POLICIES / GUIDES -- 


CREATE TABLE IF NOT EXISTS tags (
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    code VARCHAR(50) NOT NULL UNIQUE, 
    name VARCHAR(100) NOT NULL, 
    tag_group VARCHAR(100), 
    description TEXT, 
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS cancellation_policies ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    name VARCHAR(150) NOT NULL UNIQUE, 
    description TEXT, 
    voucher_bonus_percent DECIMAL(5,2) NOT NULL DEFAULT 0, 
    is_default BOOLEAN NOT NULL DEFAULT FALSE, 
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
    ) ENGINE=InnoDB; 
    
    
CREATE TABLE IF NOT EXISTS cancellation_policy_rules ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    policy_id BIGINT NOT NULL, 
    min_hours_before INT NULL, 
    max_hours_before INT NULL, 
    refund_percent DECIMAL(5,2) NOT NULL DEFAULT 0, 
    voucher_percent DECIMAL(5,2) NOT NULL DEFAULT 0, 
    fee_percent DECIMAL(5,2) NOT NULL DEFAULT 0, 
    allow_reschedule BOOLEAN NOT NULL DEFAULT FALSE, 
    notes TEXT, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_cancellation_policy_rules_policy
    FOREIGN KEY (policy_id) 
    REFERENCES cancellation_policies(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


CREATE TABLE IF NOT EXISTS guides ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    code VARCHAR(30) UNIQUE, 
    full_name VARCHAR(150) NOT NULL, 
    phone VARCHAR(20), 
    email VARCHAR(150), 
    gender ENUM('male', 'female', 'other', 'unknown') NOT NULL DEFAULT 'unknown', 
    local_region VARCHAR(120), 
    languages JSON NULL, 
    pecialties JSON NULL, 
    bio TEXT, 
    rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0, 
    total_tours_led INT NOT NULL DEFAULT 0, 
    status ENUM('active', 'inactive', 'busy', 'suspended') NOT NULL DEFAULT 'active', 
    is_local_guide BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
    ON UPDATE CURRENT_TIMESTAMP 
) ENGINE=InnoDB; 

-- 4. TOURS / ITINERARY / SCHEDULE -- 


CREATE TABLE IF NOT EXISTS tours (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(280) NOT NULL UNIQUE,
    destination_id BIGINT NOT NULL,
    cancellation_policy_id BIGINT NULL,
    short_description TEXT,
    description TEXT,
    highlights TEXT,
    inclusions TEXT,
    exclusions TEXT,
    notes TEXT,
    duration_days SMALLINT NOT NULL,
    duration_nights SMALLINT NOT NULL DEFAULT 0,
    base_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    transport_type VARCHAR(120),
    trip_mode ENUM('group', 'private', 'shared') NOT NULL DEFAULT 'group',
    difficulty_level TINYINT NOT NULL DEFAULT 1,
    activity_level TINYINT NOT NULL DEFAULT 1,
    min_age SMALLINT NULL,
    max_age SMALLINT NULL,
    min_group_size SMALLINT NOT NULL DEFAULT 1,
    max_group_size SMALLINT NOT NULL DEFAULT 50,
    is_student_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    is_family_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    is_senior_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    average_rating DECIMAL(3 , 2 ) NOT NULL DEFAULT 0,
    total_reviews INT NOT NULL DEFAULT 0,
    total_bookings INT NOT NULL DEFAULT 0,
    status ENUM('draft', 'active', 'inactive', 'archived') NOT NULL DEFAULT 'draft',
    created_by CHAR(36) NULL,
    updated_by CHAR(36) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tours_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id),
    CONSTRAINT fk_tours_policy FOREIGN KEY (cancellation_policy_id)
        REFERENCES cancellation_policies (id),
    CONSTRAINT fk_tours_created_by FOREIGN KEY (created_by)
        REFERENCES users (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_tours_updated_by FOREIGN KEY (updated_by)
        REFERENCES users (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_duration_days CHECK (duration_days > 0),
    CONSTRAINT chk_duration_nights CHECK (duration_nights >= 0),
    CONSTRAINT chk_difficulty_level CHECK (difficulty_level BETWEEN 1 AND 5),
    CONSTRAINT chk_activity_level CHECK (activity_level BETWEEN 1 AND 5)
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS tour_tags (
    tour_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tour_id , tag_id),
    CONSTRAINT fk_tour_tags_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tour_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS tour_media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    media_type ENUM('image', 'video', 'cover', 'banner') NOT NULL DEFAULT 'image',
    media_url TEXT NOT NULL,
    alt_text VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tour_media_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS tour_seasonality (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    season_name VARCHAR(100) NOT NULL,
    month_from TINYINT NULL,
    month_to TINYINT NULL,
    recommendation_score DECIMAL(5 , 2 ) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tour_seasonality_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS tour_itinerary_days (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    day_number SMALLINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    overnight_destination_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tour_day (tour_id , day_number),
    CONSTRAINT fk_tour_itinerary_days_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tour_itinerary_days_destination FOREIGN KEY (overnight_destination_id)
        REFERENCES destinations (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS itinerary_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    itinerary_day_id BIGINT NOT NULL,
    sequence_no SMALLINT NOT NULL DEFAULT 1,
    item_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    destination_id BIGINT NULL,
    location_name VARCHAR(255),
    address TEXT,
    latitude DECIMAL(10 , 7 ),
    longitude DECIMAL(10 , 7 ),
    google_map_url TEXT,
    start_time TIME NULL,
    end_time TIME NULL,
    travel_minutes_estimated INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_itinerary_items_sequence (itinerary_day_id , sequence_no),
    CONSTRAINT fk_itinerary_items_day FOREIGN KEY (itinerary_day_id)
        REFERENCES tour_itinerary_days (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_itinerary_items_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS tour_checklist_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    item_group VARCHAR(80),
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tour_checklist_items_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS tour_schedules ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
	schedule_code VARCHAR(40) UNIQUE, 
	tour_id BIGINT NOT NULL, 
	departure_at DATETIME NOT NULL, 
	return_at DATETIME NOT NULL, 
	booking_open_at DATETIME NULL, 
	booking_close_at DATETIME NULL, 
	meeting_at DATETIME NULL, 
	meeting_point_name VARCHAR(200), 
	meeting_address TEXT, 
	meeting_latitude DECIMAL(10,7), 
	meeting_longitude DECIMAL(10,7), 
	capacity_total INT NOT NULL, 
	booked_seats INT NOT NULL DEFAULT 0, 
	min_guests_to_operate INT NOT NULL DEFAULT 1, 
	adult_price DECIMAL(14,2) NOT NULL DEFAULT 0, 
	child_price DECIMAL(14,2) NOT NULL DEFAULT 0, 
	infant_price DECIMAL(14,2) NOT NULL DEFAULT 0, 
	senior_price DECIMAL(14,2) NOT NULL DEFAULT 0, 
	single_room_surcharge DECIMAL(14,2) NOT NULL DEFAULT 0, 
	transport_detail VARCHAR(255), 
	note TEXT, status ENUM('draft', 'open', 'closed', 'full', 'departed', 'completed', 'cancelled') NOT NULL DEFAULT 'draft', 
	remaining_seats INT GENERATED ALWAYS AS (capacity_total - booked_seats) STORED, 
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
	CONSTRAINT fk_tour_schedules_tour 
	FOREIGN KEY (tour_id) 
	REFERENCES tours(id) 
	ON DELETE CASCADE, 
	CONSTRAINT chk_schedule_time CHECK (return_at > departure_at), 
	CONSTRAINT chk_capacity_total CHECK (capacity_total > 0), 
	CONSTRAINT chk_booked_seats CHECK (booked_seats >= 0) 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS tour_schedule_pickup_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    point_name VARCHAR(200) NOT NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(10 , 7 ),
    longitude DECIMAL(10 , 7 ),
    pickup_at DATETIME NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tour_schedule_pickup_points_schedule FOREIGN KEY (schedule_id)
        REFERENCES tour_schedules (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS tour_schedule_guides (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    guide_role VARCHAR(80) NOT NULL DEFAULT 'main',
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schedule_guide (schedule_id , guide_id),
    CONSTRAINT fk_tour_schedule_guides_schedule FOREIGN KEY (schedule_id)
        REFERENCES tour_schedules (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tour_schedule_guides_guide FOREIGN KEY (guide_id)
        REFERENCES guides (id)
)  ENGINE=INNODB; 
-- 5. PROMOTIONS / VOUCHERS / MISSIONS / COMBO / PRODUCTS -- 
CREATE TABLE IF NOT EXISTS promotion_campaigns ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    code VARCHAR(40) NOT NULL UNIQUE, 
    name VARCHAR(200) NOT NULL, 
    description TEXT, 
    start_at DATETIME NOT NULL, 
    end_at DATETIME NOT NULL, 
    target_member_level ENUM('bronze', 'silver', 'gold', 'platinum', 'diamond') NULL, 
    conditions_json JSON NULL, 
    reward_json JSON NULL, 
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
    ON UPDATE CURRENT_TIMESTAMP 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    campaign_id BIGINT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type ENUM('percentage', 'fixed_amount', 'gift', 'cashback') NOT NULL,
    discount_value DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(14 , 2 ) NULL,
    min_order_value DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    usage_limit_total INT NULL,
    usage_limit_per_user INT NOT NULL DEFAULT 1,
    used_count INT NOT NULL DEFAULT 0,
    applicable_scope VARCHAR(50) NOT NULL DEFAULT 'all',
    applicable_tour_id BIGINT NULL,
    applicable_destination_id BIGINT NULL,
    applicable_member_level ENUM('bronze', 'silver', 'gold', 'platinum', 'diamond') NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    is_stackable BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vouchers_campaign FOREIGN KEY (campaign_id)
        REFERENCES promotion_campaigns (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_vouchers_tour FOREIGN KEY (applicable_tour_id)
        REFERENCES tours (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_vouchers_destination FOREIGN KEY (applicable_destination_id)
        REFERENCES destinations (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS voucher_user_claims (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    voucher_id BIGINT NOT NULL,
    user_id CHAR(36) NOT NULL,
    claimed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_count INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_voucher_user_claim (voucher_id , user_id),
    CONSTRAINT fk_voucher_user_claims_voucher FOREIGN KEY (voucher_id)
        REFERENCES vouchers (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_voucher_user_claims_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS mission_definitions ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
	code VARCHAR(50) NOT NULL UNIQUE, 
    name VARCHAR(200) NOT NULL, 
    description TEXT, 
    rule_json JSON NULL, 
    reward_type ENUM('voucher', 'points', 'gift') NOT NULL, 
    reward_value DECIMAL(14,2) NOT NULL DEFAULT 0, 
    reward_ref_id BIGINT NULL, 
    start_at DATETIME NULL, 
    end_at DATETIME NULL, 
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS user_missions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    mission_id BIGINT NOT NULL,
    progress DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    goal DECIMAL(14 , 2 ) NOT NULL DEFAULT 1,
    status ENUM('in_progress', 'completed', 'claimed', 'expired') NOT NULL DEFAULT 'in_progress',
    completed_at DATETIME NULL,
    claimed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_mission (user_id , mission_id),
    CONSTRAINT fk_user_missions_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_missions_mission FOREIGN KEY (mission_id)
        REFERENCES mission_definitions (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS combo_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS combo_package_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    combo_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    item_ref_id BIGINT NULL,
    item_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_combo_package_items_combo FOREIGN KEY (combo_id)
        REFERENCES combo_packages (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    product_type ENUM('gear', 'insurance', 'food', 'souvenir', 'service', 'gift') NOT NULL,
    unit_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    stock_qty INT NOT NULL DEFAULT 0,
    is_giftable BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)  ENGINE=INNODB; 


-- 6. BOOKINGS / PASSENGERS / HISTORY --


CREATE TABLE IF NOT EXISTS bookings ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    booking_code VARCHAR(50) UNIQUE, 
    user_id CHAR(36) NOT NULL, 
    tour_id BIGINT NOT NULL, 
    schedule_id BIGINT NOT NULL, 
    status ENUM('pending_payment', 'confirmed', 'checked_in', 'completed', 'cancel_requested', 'cancelled', 'refunded', 'expired') NOT NULL DEFAULT 'pending_payment', 
    payment_status ENUM('unpaid', 'partial', 'paid', 'failed', 'refunded', 'chargeback') NOT NULL DEFAULT 'unpaid', 
    contact_name VARCHAR(150) NOT NULL, 
    contact_phone VARCHAR(20) NOT NULL, 
    contact_email VARCHAR(150), 
    adults INT NOT NULL DEFAULT 1, 
    children INT NOT NULL DEFAULT 0, 
    infants INT NOT NULL DEFAULT 0, 
    seniors INT NOT NULL DEFAULT 0, 
    people_count INT GENERATED ALWAYS AS (adults + children + infants + seniors) STORED, 
    seat_count INT GENERATED ALWAYS AS (adults + children + seniors) STORED, 
    subtotal_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    voucher_discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    loyalty_discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    addon_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    final_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    currency CHAR(3) NOT NULL DEFAULT 'VND', 
    voucher_id BIGINT NULL, 
    combo_id BIGINT NULL, 
    booking_source VARCHAR(30) NOT NULL DEFAULT 'app', 
    special_requests TEXT, 
    cancel_reason TEXT, 
    cancelled_at DATETIME NULL, 
    completed_at DATETIME NULL, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    CONSTRAINT fk_bookings_user 
    FOREIGN KEY (user_id) 
    REFERENCES users(id), 
    CONSTRAINT fk_bookings_tour 
    FOREIGN KEY (tour_id) 
    REFERENCES tours(id), 
    CONSTRAINT fk_bookings_schedule 
    FOREIGN KEY (schedule_id) 
    REFERENCES tour_schedules(id), 
    CONSTRAINT fk_bookings_voucher 
    FOREIGN KEY (voucher_id) 
    REFERENCES vouchers(id) 
    ON DELETE SET NULL, 
    CONSTRAINT fk_bookings_combo 
    FOREIGN KEY (combo_id) 
    REFERENCES combo_packages(id) ON DELETE SET NULL, 
    CONSTRAINT chk_booking_people CHECK ((adults + children + infants + seniors) > 0) 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS booking_passengers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    passenger_type ENUM('adult', 'child', 'infant', 'senior') NOT NULL DEFAULT 'adult',
    full_name VARCHAR(150) NOT NULL,
    gender ENUM('male', 'female', 'other', 'unknown') NOT NULL DEFAULT 'unknown',
    date_of_birth DATE NULL,
    identity_no VARCHAR(50),
    passport_no VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(150),
    emergency_contact_name VARCHAR(150),
    emergency_contact_phone VARCHAR(20),
    medical_note TEXT,
    meal_note TEXT,
    seat_note TEXT,
    checked_in BOOLEAN NOT NULL DEFAULT FALSE,
    checked_in_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_passengers_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS booking_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    old_status ENUM('pending_payment', 'confirmed', 'checked_in', 'completed', 'cancel_requested', 'cancelled', 'refunded', 'expired') NULL,
    new_status ENUM('pending_payment', 'confirmed', 'checked_in', 'completed', 'cancel_requested', 'cancelled', 'refunded', 'expired') NOT NULL,
    changed_by CHAR(36) NULL,
    change_reason TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_status_history_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_status_history_user FOREIGN KEY (changed_by)
        REFERENCES users (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;CREATE TABLE IF NOT EXISTS booking_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    line_total DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    is_free_gift BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_products_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_products_product FOREIGN KEY (product_id)
        REFERENCES products (id)
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS booking_combo_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    combo_id BIGINT NOT NULL,
    unit_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    final_price DECIMAL(14 , 2 ) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_combo_items_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_combo_items_combo FOREIGN KEY (combo_id)
        REFERENCES combo_packages (id)
)  ENGINE=INNODB; 

-- 7. PAYMENTS / REFUNDS -- 

CREATE TABLE IF NOT EXISTS payments ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    payment_code VARCHAR(50) UNIQUE, 
    booking_id BIGINT NOT NULL, 
    payment_method ENUM('cash', 'bank_transfer', 'credit_card', 'e_wallet', 'qr', 'gateway') NOT NULL, 
    provider VARCHAR(100), 
    transaction_ref VARCHAR(150), 
    amount DECIMAL(14,2) NOT NULL, 
    currency CHAR(3) NOT NULL DEFAULT 'VND', status ENUM('unpaid', 'partial', 'paid', 'failed', 'refunded', 'chargeback') NOT NULL DEFAULT 'unpaid', 
    paid_at DATETIME NULL, 
    request_payload JSON NULL, 
    response_payload JSON NULL, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    CONSTRAINT fk_payments_booking 
    FOREIGN KEY (booking_id) 
    REFERENCES bookings(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS refund_requests ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    refund_code VARCHAR(50) UNIQUE, 
    booking_id BIGINT NOT NULL, 
    requested_by CHAR(36) NULL, 
    reason_type VARCHAR(100), 
    reason_detail TEXT, 
    requested_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    quoted_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    approved_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    voucher_offer_amount DECIMAL(14,2) NOT NULL DEFAULT 0, 
    refund_method ENUM('original_method', 'bank_transfer', 'wallet', 'voucher') NULL, 
    status ENUM('requested', 'quoted', 'approved', 'rejected', 'processing', 'completed', 'cancelled') NOT NULL DEFAULT 'requested',
    policy_snapshot JSON NULL, 
    processed_by CHAR(36) NULL, 
    processed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    CONSTRAINT fk_refund_requests_booking
    FOREIGN KEY (booking_id) 
    REFERENCES bookings(id) ON DELETE CASCADE, 
    CONSTRAINT fk_refund_requests_user_requested 
    FOREIGN KEY (requested_by) 
    REFERENCES users(id) ON DELETE SET NULL, 
    CONSTRAINT fk_refund_requests_user_processed 
    FOREIGN KEY (processed_by) 
    REFERENCES users(id) ON DELETE SET NULL 
) ENGINE=InnoDB; 


-- 8. REVIEWS / ANALYSIS -- 


CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL UNIQUE,
    user_id CHAR(36) NOT NULL,
    tour_id BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    overall_rating TINYINT NOT NULL,
    title VARCHAR(255),
    content TEXT,
    sentiment ENUM('positive', 'neutral', 'negative', 'mixed') NOT NULL DEFAULT 'neutral',
    would_recommend BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reviews_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reviews_schedule FOREIGN KEY (schedule_id)
        REFERENCES tour_schedules (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_reviews_rating CHECK (overall_rating BETWEEN 1 AND 5)
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS review_aspects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    aspect_name VARCHAR(100) NOT NULL,
    aspect_rating TINYINT NOT NULL,
    comment TEXT,
    UNIQUE KEY uk_review_aspect (review_id , aspect_name),
    CONSTRAINT fk_review_aspects_review FOREIGN KEY (review_id)
        REFERENCES reviews (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_review_aspects_rating CHECK (aspect_rating BETWEEN 1 AND 5)
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS review_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    staff_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_replies_review FOREIGN KEY (review_id)
        REFERENCES reviews (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_review_replies_staff FOREIGN KEY (staff_id)
        REFERENCES users (id)
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS review_analysis ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    review_id BIGINT NOT NULL UNIQUE, 
    positive_points JSON NULL, 
    negative_points JSON NULL, 
    keywords JSON NULL, 
    summary TEXT, 
    processed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_review_analysis_review 
    FOREIGN KEY (review_id) 
    REFERENCES reviews(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 


-- 9. WEATHER / CROWD / ROUTE -- 


CREATE TABLE IF NOT EXISTS weather_forecasts ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
	destination_id BIGINT NOT NULL, 
	forecast_date DATE NOT NULL, 
	weather_code VARCHAR(50), 
	summary VARCHAR(255), 
	temp_min DECIMAL(5,2), 
	temp_max DECIMAL(5,2), 
	humidity_percent DECIMAL(5,2), 
	wind_speed DECIMAL(6,2), 
	rain_probability DECIMAL(5,2), 
	source_name VARCHAR(100), 
	raw_payload JSON NULL, 
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	UNIQUE KEY uk_weather_forecast (destination_id, forecast_date), 
	CONSTRAINT fk_weather_forecasts_destination 
	FOREIGN KEY (destination_id) 
	REFERENCES destinations(id) 
	ON DELETE CASCADE 
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS weather_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    destination_id BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    severity ENUM('info', 'watch', 'warning', 'danger') NOT NULL,
    alert_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    action_advice TEXT,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_weather_alerts_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_weather_alerts_schedule FOREIGN KEY (schedule_id)
        REFERENCES tour_schedules (id)
        ON DELETE SET NULL
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS crowd_predictions ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
	destination_id BIGINT NOT NULL, 
	prediction_date DATE NOT NULL, 
	crowd_level ENUM('low', 'medium', 'high', 'very_high') NOT NULL, 
	predicted_visitors INT NULL, confidence_score DECIMAL(5,2), 
	reasons_json JSON NULL, 
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	UNIQUE KEY uk_crowd_prediction (destination_id, prediction_date), 
	CONSTRAINT fk_crowd_predictions_destination 
	FOREIGN KEY (destination_id) 
	REFERENCES destinations(id) 
	ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS route_estimates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_label VARCHAR(255),
    to_label VARCHAR(255),
    from_latitude DECIMAL(10 , 7 ),
    from_longitude DECIMAL(10 , 7 ),
    to_latitude DECIMAL(10 , 7 ),
    to_longitude DECIMAL(10 , 7 ),
    distance_km DECIMAL(10 , 2 ),
    duration_minutes INT,
    google_map_url TEXT,
    source_name VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
)  ENGINE=INNODB; 



-- 10. NOTIFICATIONS -- 


CREATE TABLE IF NOT EXISTS notifications ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    user_id CHAR(36) NULL, 
    notification_type ENUM('booking', 'payment', 'weather', 'promotion', 'schedule_change', 'reminder', 'system', 'chat', 'destination_follow') NOT NULL, channel ENUM('push', 'email', 'sms', 'in_app') NOT NULL DEFAULT 'in_app', 
    title VARCHAR(255) NOT NULL, 
    body TEXT NOT NULL, 
    reference_type VARCHAR(80), 
    reference_id BIGINT NULL, 
    payload JSON NULL, 
    scheduled_at DATETIME NULL, 
    sent_at DATETIME NULL, 
    read_at DATETIME NULL, 
    is_broadcast BOOLEAN NOT NULL DEFAULT FALSE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_notifications_user 
    FOREIGN KEY (user_id) 
    REFERENCES users(id) 
    ON DELETE CASCADE 
) ENGINE=InnoDB; 



-- 11. SUPPORT CHAT / TOUR MEMBER CHAT -- 


CREATE TABLE IF NOT EXISTS support_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_code VARCHAR(50) UNIQUE,
    user_id CHAR(36) NOT NULL,
    assigned_staff_id CHAR(36) NULL,
    status ENUM('open', 'waiting_staff', 'waiting_customer', 'resolved', 'closed') NOT NULL DEFAULT 'open',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME NULL,
    last_message_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_support_sessions_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_support_sessions_staff FOREIGN KEY (assigned_staff_id)
        REFERENCES users (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS support_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    sender_type ENUM('customer', 'staff', 'bot', 'system') NOT NULL,
    sender_user_id CHAR(36) NULL,
    message_text TEXT NOT NULL,
    attachment_url TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_support_messages_session FOREIGN KEY (session_id)
        REFERENCES support_sessions (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_support_messages_user FOREIGN KEY (sender_user_id)
        REFERENCES users (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS schedule_chat_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL UNIQUE,
    room_name VARCHAR(200) NOT NULL,
    visibility ENUM('all_members', 'staff_only') NOT NULL DEFAULT 'all_members',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_chat_rooms_schedule FOREIGN KEY (schedule_id)
        REFERENCES tour_schedules (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS schedule_chat_room_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    user_id CHAR(36) NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_schedule_chat_member (room_id , user_id),
    CONSTRAINT fk_schedule_chat_room_members_room FOREIGN KEY (room_id)
        REFERENCES schedule_chat_rooms (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_schedule_chat_room_members_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS schedule_chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    sender_user_id CHAR(36) NOT NULL,
    message_text TEXT NOT NULL,
    attachment_url TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_chat_messages_room FOREIGN KEY (room_id)
        REFERENCES schedule_chat_rooms (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_schedule_chat_messages_user FOREIGN KEY (sender_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 

-- 12. PASSPORT / BADGES / CHECKIN -- 


CREATE TABLE IF NOT EXISTS travel_passports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL UNIQUE,
    passport_no VARCHAR(50) UNIQUE,
    total_tours INT NOT NULL DEFAULT 0,
    total_destinations INT NOT NULL DEFAULT 0,
    total_checkins INT NOT NULL DEFAULT 0,
    last_trip_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_travel_passports_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS badge_definitions ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    code VARCHAR(50) NOT NULL UNIQUE, 
    name VARCHAR(150) NOT NULL, 
    description TEXT, 
    icon_url TEXT, 
    condition_json JSON NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
) ENGINE=InnoDB;
    
    
CREATE TABLE IF NOT EXISTS passport_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    passport_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    unlocked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_passport_badge (passport_id , badge_id),
    CONSTRAINT fk_passport_badges_passport FOREIGN KEY (passport_id)
        REFERENCES travel_passports (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_passport_badges_badge FOREIGN KEY (badge_id)
        REFERENCES badge_definitions (id)
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS user_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    booking_id BIGINT NULL,
    destination_id BIGINT NULL,
    checkin_latitude DECIMAL(10 , 7 ),
    checkin_longitude DECIMAL(10 , 7 ),
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_checkins_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_checkins_booking FOREIGN KEY (booking_id)
        REFERENCES bookings (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_user_checkins_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id)
        ON DELETE SET NULL
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS passport_visited_destinations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    passport_id BIGINT NOT NULL,
    destination_id BIGINT NOT NULL,
    first_booking_id BIGINT NULL,
    first_visited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_visited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_passport_visited_destination (passport_id , destination_id),
    CONSTRAINT fk_passport_visited_destinations_passport FOREIGN KEY (passport_id)
        REFERENCES travel_passports (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_passport_visited_destinations_destination FOREIGN KEY (destination_id)
        REFERENCES destinations (id),
    CONSTRAINT fk_passport_visited_destinations_booking FOREIGN KEY (first_booking_id)
        REFERENCES bookings (id)
        ON DELETE SET NULL
)  ENGINE=INNODB; 


-- 13. USER BEHAVIOR / WISHLIST / RECOMMENDATION -- 


CREATE TABLE IF NOT EXISTS user_tour_views (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    tour_id BIGINT NOT NULL,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_tour_views_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_tour_views_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE
)  ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS wishlist_tours (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL,
    tour_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wishlist_tour (user_id , tour_id),
    CONSTRAINT fk_wishlist_tours_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_tours_tour FOREIGN KEY (tour_id)
        REFERENCES tours (id)
        ON DELETE CASCADE
)  ENGINE=INNODB; 


CREATE TABLE IF NOT EXISTS recommendation_logs ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    user_id CHAR(36) NULL, 
    requested_tag VARCHAR(100), 
    requested_budget ENUM('low', 'medium', 'high', 'luxury') NULL, 
    requested_trip_mode ENUM('group', 'private', 'shared') NULL, 
    requested_people_count INT NULL, 
    requested_departure_at DATETIME NULL, 
    generated_result JSON NULL, 
    scoring_detail JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_recommendation_logs_user 
    FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE SET NULL 
) ENGINE=InnoDB; 


-- 14. AUDIT LOG --
CREATE TABLE IF NOT EXISTS audit_logs ( 
	id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    actor_user_id CHAR(36) NULL, 
    action_name VARCHAR(120) NOT NULL, 
    entity_name VARCHAR(120) NOT NULL, 
    entity_id VARCHAR(120) NOT NULL, 
    old_data JSON NULL, 
    new_data JSON NULL, 
    ip_address VARCHAR(45), 
    user_agent TEXT, 
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    CONSTRAINT fk_audit_logs_user 
    FOREIGN KEY (actor_user_id) 
    REFERENCES users(id) 
    ON DELETE SET NULL 
) ENGINE=InnoDB; 


-- 15. INDEXES -- 


CREATE INDEX idx_users_status ON users(status); 
CREATE INDEX idx_users_user_category ON users(user_category); 
CREATE INDEX idx_roles_code_active ON roles(code, is_active); 
CREATE INDEX idx_permissions_module_action ON permissions(module_name, action_name); 
CREATE INDEX idx_users_member_level ON users(member_level); 
CREATE INDEX idx_destinations_province ON destinations(province); 
CREATE INDEX idx_destinations_region ON destinations(region); 
CREATE INDEX idx_tours_destination_id ON tours(destination_id); 
CREATE INDEX idx_tours_status_featured ON tours(status, is_featured); 
CREATE INDEX idx_tour_tags_tag_id ON tour_tags(tag_id); 
CREATE INDEX idx_tour_schedules_tour_id ON tour_schedules(tour_id); 
CREATE INDEX idx_tour_schedules_departure_at ON tour_schedules(departure_at); 
CREATE INDEX idx_tour_schedules_status ON tour_schedules(status); 
CREATE INDEX idx_bookings_user_id ON bookings(user_id); 
CREATE INDEX idx_bookings_tour_id ON bookings(tour_id); 
CREATE INDEX idx_bookings_schedule_id ON bookings(schedule_id); 
CREATE INDEX idx_bookings_status ON bookings(status); 
CREATE INDEX idx_payments_booking_id ON payments(booking_id); 
CREATE INDEX idx_refund_requests_booking_id ON refund_requests(booking_id); 
CREATE INDEX idx_reviews_tour_id ON reviews(tour_id); 
CREATE INDEX idx_reviews_user_id ON reviews(user_id); 
CREATE INDEX idx_weather_forecasts_destination_date ON weather_forecasts(destination_id, forecast_date); 
CREATE INDEX idx_weather_alerts_destination_id ON weather_alerts(destination_id); 
CREATE INDEX idx_crowd_predictions_destination_date ON crowd_predictions(destination_id, prediction_date); 
CREATE INDEX idx_notifications_user_read ON notifications(user_id, read_at); 
CREATE INDEX idx_support_sessions_user_id ON support_sessions(user_id); 
CREATE INDEX idx_support_messages_session_id ON support_messages(session_id); 
CREATE INDEX idx_travel_passports_user_id ON travel_passports(user_id); 
CREATE INDEX idx_user_checkins_user_id ON user_checkins(user_id); 
CREATE INDEX idx_user_tour_views_user_id ON user_tour_views(user_id); 
CREATE INDEX idx_wishlist_tours_user_id ON wishlist_tours(user_id); 


-- 16. VIEWS -- 


CREATE OR REPLACE VIEW v_tour_public_summary AS
    SELECT 
        t.id,
        t.code,
        t.name,
        t.slug,
        d.name AS destination_name,
        d.province,
        t.duration_days,
        t.duration_nights,
        t.base_price,
        t.currency,
        t.transport_type,
        t.trip_mode,
        t.is_student_friendly,
        t.is_family_friendly,
        t.is_senior_friendly,
        t.average_rating,
        t.total_reviews,
        t.total_bookings,
        t.status
    FROM
        tours t
            JOIN
        destinations d ON d.id = t.destination_id;CREATE OR REPLACE VIEW v_schedule_availability AS
    SELECT 
        ts.id,
        ts.schedule_code,
        ts.tour_id,
        t.name AS tour_name,
        ts.departure_at,
        ts.return_at,
        ts.capacity_total,
        ts.booked_seats,
        ts.remaining_seats,
        ts.status
    FROM
        tour_schedules ts
            JOIN
        tours t ON t.id = ts.tour_id;CREATE OR REPLACE VIEW v_popular_tours_by_tag AS
    SELECT 
        tg.id AS tag_id,
        tg.name AS tag_name,
        t.id AS tour_id,
        t.name AS tour_name,
        COUNT(b.id) AS successful_bookings,
        ROUND(IFNULL(AVG(r.overall_rating), 0), 2) AS avg_rating
    FROM
        tags tg
            JOIN
        tour_tags tt ON tt.tag_id = tg.id
            JOIN
        tours t ON t.id = tt.tour_id
            LEFT JOIN
        bookings b ON b.tour_id = t.id
            AND b.status IN ('confirmed' , 'checked_in', 'completed')
            LEFT JOIN
        reviews r ON r.tour_id = t.id
    GROUP BY tg.id , tg.name , t.id , t.name; 
    
    


CREATE OR REPLACE VIEW v_user_primary_roles AS
    SELECT 
        u.id AS user_id,
        u.full_name,
        u.email,
        u.phone,
        u.status,
        u.user_category,
        r.code AS primary_role_code,
        r.name AS primary_role_name,
        r.hierarchy_level
    FROM
        users u
            LEFT JOIN
        user_roles ur ON ur.user_id = u.id AND ur.is_primary = TRUE
            LEFT JOIN
        roles r ON r.id = ur.role_id;

CREATE OR REPLACE VIEW v_user_effective_permissions AS
    SELECT DISTINCT
        u.id AS user_id,
        u.full_name,
        r.code AS role_code,
        p.code AS permission_code,
        p.module_name,
        p.action_name
    FROM
        users u
            JOIN
        user_roles ur ON ur.user_id = u.id
            JOIN
        roles r ON r.id = ur.role_id AND r.is_active = TRUE
            JOIN
        role_permissions rp ON rp.role_id = r.id
            JOIN
        permissions p ON p.id = rp.permission_id AND p.is_active = TRUE; 

-- 17. FUNCTIONS / PROCEDURES -- 
DELIMITER $$ CREATE FUNCTION fn_generate_code(p_prefix VARCHAR(10)) 
RETURNS VARCHAR(50) DETERMINISTIC BEGIN RETURN CONCAT( 
	UPPER(p_prefix), 
    DATE_FORMAT(NOW(), '%y%m%d%H%i%s'), 
    UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6)) 
); END$$ 
CREATE PROCEDURE sp_sync_schedule_booked_seats(IN p_schedule_id BIGINT) 
BEGIN UPDATE tour_schedules s SET s.booked_seats = IFNULL(( SELECT SUM(b.adults + b.children + b.seniors) 
FROM bookings b 
WHERE b.schedule_id = p_schedule_id AND b.status 
IN ('pending_payment', 'confirmed', 'checked_in', 'completed') ), 0) 
WHERE s.id = p_schedule_id; 
END$$ 
CREATE PROCEDURE sp_sync_tour_booking_stats(IN p_tour_id BIGINT)
BEGIN UPDATE tours t SET t.total_bookings = IFNULL(( SELECT COUNT(*) 
FROM bookings b WHERE b.tour_id = p_tour_id AND b.status IN ('confirmed', 'checked_in', 'completed') ), 0) 
WHERE t.id = p_tour_id; END$$ 

CREATE PROCEDURE sp_sync_tour_rating(IN p_tour_id BIGINT) 
BEGIN UPDATE tours t SET t.average_rating = IFNULL(( SELECT ROUND(AVG(r.overall_rating), 2) 
FROM reviews r WHERE r.tour_id = p_tour_id ), 0), 
t.total_reviews = IFNULL(( SELECT COUNT(*) 
FROM reviews r 
WHERE r.tour_id = p_tour_id ), 0) 
WHERE t.id = p_tour_id; 

END$$ 

CREATE PROCEDURE sp_get_refund_quote(IN p_booking_id BIGINT) 
BEGIN SELECT b.id AS booking_id, b.booking_code, cp.id AS policy_id, cp.name AS policy_name, 
TIMESTAMPDIFF(HOUR, NOW(), ts.departure_at) AS hours_before_departure, cpr.refund_percent, cpr.voucher_percent, cpr.allow_reschedule, 
ROUND(b.final_amount * cpr.refund_percent / 100, 2) AS refundable_amount, 
ROUND(b.final_amount * cpr.voucher_percent / 100, 2) AS voucher_offer_amount FROM bookings b JOIN tours t ON t.id = b.tour_id JOIN tour_schedules ts ON ts.id = b.schedule_id LEFT JOIN cancellation_policies cp ON cp.id = t.cancellation_policy_id 
LEFT JOIN cancellation_policy_rules cpr ON cpr.policy_id = cp.id 
AND TIMESTAMPDIFF(HOUR, NOW(), ts.departure_at) >= IFNULL(cpr.min_hours_before, 0) 
AND ( cpr.max_hours_before IS NULL OR TIMESTAMPDIFF(HOUR, NOW(), ts.departure_at) < cpr.max_hours_before ) 
WHERE b.id = p_booking_id ORDER BY cpr.min_hours_before DESC LIMIT 1; END$$ 
-- ========================================================= -- 18. TRIGGERS -- ========================================================= 
CREATE TRIGGER trg_users_before_insert BEFORE INSERT ON users FOR EACH ROW BEGIN IF NEW.id IS NULL OR NEW.id = '' THEN SET NEW.id = UUID(); END IF; END$$ CREATE TRIGGER trg_guides_before_insert BEFORE INSERT ON guides FOR EACH ROW BEGIN IF NEW.code IS NULL OR NEW.code = '' THEN SET NEW.code = fn_generate_code('GD'); END IF; END$$ CREATE TRIGGER trg_tour_schedules_before_insert BEFORE INSERT ON tour_schedules FOR EACH ROW BEGIN IF NEW.schedule_code IS NULL OR NEW.schedule_code = '' THEN SET NEW.schedule_code = fn_generate_code('SCH'); END IF; END$$ CREATE TRIGGER trg_bookings_before_insert BEFORE INSERT ON bookings FOR EACH ROW BEGIN DECLARE v_capacity INT DEFAULT 0; DECLARE v_booked INT DEFAULT 0; DECLARE v_new_seats INT DEFAULT 0; IF NEW.booking_code IS NULL OR NEW.booking_code = '' THEN SET NEW.booking_code = fn_generate_code('BK'); END IF; SET v_new_seats = NEW.adults + NEW.children + NEW.seniors;SELECT 
    capacity_total, booked_seats
INTO v_capacity , v_booked FROM
    tour_schedules
WHERE
    id = NEW.schedule_id; IF v_capacity IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Schedule does not exist'; END IF; IF NEW.status IN ('pending_payment', 'confirmed', 'checked_in', 'completed') THEN IF (v_booked + v_new_seats) > v_capacity THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Over capacity for schedule'; END IF; END IF; END$$ CREATE TRIGGER trg_bookings_before_update BEFORE UPDATE ON bookings FOR EACH ROW BEGIN DECLARE v_capacity INT DEFAULT 0; DECLARE v_booked INT DEFAULT 0; DECLARE v_new_seats INT DEFAULT 0; SET v_new_seats = NEW.adults + NEW.children + NEW.seniors;SELECT 
    capacity_total
INTO v_capacity FROM
    tour_schedules
WHERE
    id = NEW.schedule_id;SELECT 
    IFNULL(SUM(adults + children + seniors), 0)
INTO v_booked FROM
    bookings
WHERE
    schedule_id = NEW.schedule_id
        AND id <> OLD.id
        AND status IN ('pending_payment' , 'confirmed', 'checked_in', 'completed'); IF NEW.status IN ('pending_payment', 'confirmed', 'checked_in', 'completed') THEN IF (v_booked + v_new_seats) > v_capacity THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Over capacity for schedule'; END IF; END IF; END$$ CREATE TRIGGER trg_bookings_after_insert AFTER INSERT ON bookings FOR EACH ROW BEGIN INSERT INTO booking_status_history(booking_id, old_status, new_status, created_at) VALUES (NEW.id, NULL, NEW.status, NOW()); CALL sp_sync_schedule_booked_seats(NEW.schedule_id); CALL sp_sync_tour_booking_stats(NEW.tour_id); END$$ CREATE TRIGGER trg_bookings_after_update AFTER UPDATE ON bookings FOR EACH ROW BEGIN IF OLD.status <> NEW.status THEN INSERT INTO booking_status_history(booking_id, old_status, new_status, created_at) VALUES (NEW.id, OLD.status, NEW.status, NOW()); END IF; CALL sp_sync_schedule_booked_seats(OLD.schedule_id); IF NEW.schedule_id <> OLD.schedule_id THEN CALL sp_sync_schedule_booked_seats(NEW.schedule_id); END IF; CALL sp_sync_tour_booking_stats(OLD.tour_id); IF NEW.tour_id <> OLD.tour_id THEN CALL sp_sync_tour_booking_stats(NEW.tour_id); END IF; IF OLD.status <> 'completed' AND NEW.status = 'completed' THEN UPDATE travel_passports tp SET tp.total_tours = tp.total_tours + 1, tp.last_trip_at = NOW() WHERE tp.user_id = NEW.user_id; INSERT INTO passport_visited_destinations ( passport_id, destination_id, first_booking_id, first_visited_at, last_visited_at ) SELECT tp.id, t.destination_id, NEW.id, NOW(), NOW() FROM travel_passports tp JOIN tours t ON t.id = NEW.tour_id WHERE tp.user_id = NEW.user_id ON DUPLICATE KEY UPDATE last_visited_at = VALUES(last_visited_at);UPDATE travel_passports tp 
SET 
    tp.total_destinations = (SELECT 
            COUNT(*)
        FROM
            passport_visited_destinations pvd
        WHERE
            pvd.passport_id = tp.id)
WHERE
    tp.user_id = NEW.user_id;UPDATE users 
SET 
    total_spent = total_spent + NEW.final_amount,
    loyalty_points = loyalty_points + FLOOR(NEW.final_amount / 1000000)
WHERE
    id = NEW.user_id; END IF; END$$ CREATE TRIGGER trg_bookings_after_delete AFTER DELETE ON bookings FOR EACH ROW BEGIN CALL sp_sync_schedule_booked_seats(OLD.schedule_id); CALL sp_sync_tour_booking_stats(OLD.tour_id); END$$ CREATE TRIGGER trg_payments_before_insert BEFORE INSERT ON payments FOR EACH ROW BEGIN IF NEW.payment_code IS NULL OR NEW.payment_code = '' THEN SET NEW.payment_code = fn_generate_code('PM'); END IF; END$$ CREATE TRIGGER trg_refund_requests_before_insert BEFORE INSERT ON refund_requests FOR EACH ROW BEGIN IF NEW.refund_code IS NULL OR NEW.refund_code = '' THEN SET NEW.refund_code = fn_generate_code('RF'); END IF; END$$ CREATE TRIGGER trg_support_sessions_before_insert BEFORE INSERT ON support_sessions FOR EACH ROW BEGIN IF NEW.session_code IS NULL OR NEW.session_code = '' THEN SET NEW.session_code = fn_generate_code('CS'); END IF; END$$ CREATE TRIGGER trg_users_after_insert AFTER INSERT ON users FOR EACH ROW BEGIN DECLARE v_user_role_id BIGINT; INSERT INTO travel_passports(user_id, passport_no) VALUES (NEW.id, fn_generate_code('TVP')); SELECT id INTO v_user_role_id FROM roles WHERE code = 'USER' LIMIT 1; IF v_user_role_id IS NOT NULL THEN INSERT IGNORE INTO user_roles(user_id, role_id, is_primary, assigned_at, note) VALUES (NEW.id, v_user_role_id, TRUE, NOW(), 'Auto assign default USER role'); END IF; END$$ CREATE TRIGGER trg_reviews_after_insert AFTER INSERT ON reviews FOR EACH ROW BEGIN CALL sp_sync_tour_rating(NEW.tour_id); END$$ CREATE TRIGGER trg_reviews_after_update AFTER UPDATE ON reviews FOR EACH ROW BEGIN CALL sp_sync_tour_rating(OLD.tour_id); IF NEW.tour_id <> OLD.tour_id THEN CALL sp_sync_tour_rating(NEW.tour_id); END IF; END$$ CREATE TRIGGER trg_reviews_after_delete AFTER DELETE ON reviews FOR EACH ROW BEGIN CALL sp_sync_tour_rating(OLD.tour_id); END$$ CREATE TRIGGER trg_user_checkins_after_insert AFTER INSERT ON user_checkins FOR EACH ROW BEGIN UPDATE travel_passports tp SET tp.total_checkins = ( SELECT COUNT(*) FROM user_checkins uc WHERE uc.user_id = NEW.user_id ) WHERE tp.user_id = NEW.user_id; END$$ DELIMITER ; 
-- ========================================================= -- 19. DEFAULT DATA -- ========================================================= 

INSERT IGNORE INTO roles (code, name, description, role_scope, hierarchy_level, is_system_role, is_active) 
VALUES ('SUPER_ADMIN', 'Super Admin', 'Toàn quyền hệ thống, quản lý role và permission', 'SYSTEM', 100, TRUE, TRUE), ('ADMIN', 'Admin', 'Quản trị vận hành toàn hệ thống nhưng không toàn quyền như Super Admin', 'BACKOFFICE', 80, TRUE, TRUE), ('CONTENT_EDITOR', 'Content Editor', 'Quản lý nội dung điểm đến, tour, media, bài viết mô tả', 'BACKOFFICE', 60, TRUE, TRUE), ('FIELD_STAFF', 'Field Staff', 'Nhân sự khảo sát thực địa, cập nhật dữ liệu thực tế, check điểm đón', 'BACKOFFICE', 50, TRUE, TRUE), ('OPERATOR', 'Operator', 'Điều phối lịch khởi hành, booking, support, refund cơ bản', 'BACKOFFICE', 55, TRUE, TRUE), ('USER', 'User', 'Khách hàng sử dụng ứng dụng đặt tour', 'CUSTOMER', 10, TRUE, TRUE); 

INSERT IGNORE INTO permissions (code, name, module_name, action_name, description) 
VALUES ('dashboard.view', 'Xem dashboard', 'dashboard', 'view', 'Truy cập dashboard quản trị'), ('user.view', 'Xem người dùng', 'user', 'view', 'Xem danh sách và chi tiết người dùng'), ('user.create', 'Tạo người dùng', 'user', 'create', 'Tạo tài khoản người dùng nội bộ hoặc khách hàng'), ('user.update', 'Sửa người dùng', 'user', 'update', 'Cập nhật hồ sơ người dùng'), ('user.delete', 'Xóa người dùng', 'user', 'delete', 'Xóa mềm hoặc vô hiệu người dùng'), ('user.block', 'Khóa người dùng', 'user', 'block', 'Khóa / mở khóa tài khoản người dùng'), ('role.view', 'Xem role', 'role', 'view', 'Xem danh sách role'), ('role.create', 'Tạo role', 'role', 'create', 'Tạo role mới'), ('role.update', 'Sửa role', 'role', 'update', 'Chỉnh sửa role'), ('role.delete', 'Xóa role', 'role', 'delete', 'Xóa role không còn sử dụng'), ('role.assign', 'Gán role', 'role', 'assign', 'Gán role cho user'), ('permission.view', 'Xem permission', 'permission', 'view', 'Xem danh sách permission'), ('permission.assign', 'Gán permission', 'permission', 'assign', 'Gán permission cho role'), ('destination.view', 'Xem destination', 'destination', 'view', 'Xem điểm đến'), ('destination.create', 'Tạo destination', 'destination', 'create', 'Tạo điểm đến'), ('destination.update', 'Sửa destination', 'destination', 'update', 'Sửa điểm đến'), ('destination.delete', 'Xóa destination', 'destination', 'delete', 'Xóa / ẩn điểm đến'), ('destination.publish', 'Xuất bản destination', 'destination', 'publish', 'Kích hoạt / hiển thị điểm đến'), ('content.view', 'Xem nội dung', 'content', 'view', 'Xem nội dung CMS'), ('content.create', 'Tạo nội dung', 'content', 'create', 'Tạo nội dung mô tả / media / tips / events'), ('content.update', 'Sửa nội dung', 'content', 'update', 'Cập nhật nội dung'), ('content.delete', 'Xóa nội dung', 'content', 'delete', 'Xóa nội dung'), ('content.publish', 'Xuất bản nội dung', 'content', 'publish', 'Duyệt và xuất bản nội dung'), ('tour.view', 'Xem tour', 'tour', 'view', 'Xem tour'), ('tour.create', 'Tạo tour', 'tour', 'create', 'Tạo tour mới'), ('tour.update', 'Sửa tour', 'tour', 'update', 'Cập nhật tour'), ('tour.delete', 'Xóa tour', 'tour', 'delete', 'Xóa / ẩn tour'), ('tour.publish', 'Xuất bản tour', 'tour', 'publish', 'Kích hoạt tour'), ('schedule.view', 'Xem lịch khởi hành', 'schedule', 'view', 'Xem schedule'), ('schedule.create', 'Tạo lịch khởi hành', 'schedule', 'create', 'Tạo schedule'), ('schedule.update', 'Sửa lịch khởi hành', 'schedule', 'update', 'Cập nhật schedule'), ('schedule.close', 'Đóng / mở lịch khởi hành', 'schedule', 'close', 'Đóng, mở, hủy schedule'), ('guide.assign', 'Phân công hướng dẫn viên', 'guide', 'assign', 'Gán guide cho schedule'), ('booking.view', 'Xem booking', 'booking', 'view', 'Xem booking'), ('booking.create', 'Tạo booking', 'booking', 'create', 'Tạo booking'), ('booking.update', 'Sửa booking', 'booking', 'update', 'Chỉnh sửa booking'), ('booking.cancel', 'Hủy booking', 'booking', 'cancel', 'Thực hiện hủy booking'), ('booking.checkin', 'Check-in booking', 'booking', 'checkin', 'Check-in khách đi tour'), ('payment.view', 'Xem thanh toán', 'payment', 'view', 'Xem thanh toán'), ('payment.create', 'Tạo thanh toán', 'payment', 'create', 'Ghi nhận thanh toán'), ('payment.update', 'Sửa thanh toán', 'payment', 'update', 'Cập nhật trạng thái thanh toán'), ('refund.view', 'Xem hoàn tiền', 'refund', 'view', 'Xem refund request'), ('refund.create', 'Tạo yêu cầu hoàn tiền', 'refund', 'create', 'Tạo refund request'), ('refund.approve', 'Duyệt hoàn tiền', 'refund', 'approve', 'Duyệt hoàn tiền'), ('refund.reject', 'Từ chối hoàn tiền', 'refund', 'reject', 'Từ chối hoàn tiền'), ('refund.process', 'Xử lý hoàn tiền', 'refund', 'process', 'Xử lý hoàn tiền thực tế'), ('voucher.view', 'Xem voucher', 'voucher', 'view', 'Xem voucher'), ('voucher.create', 'Tạo voucher', 'voucher', 'create', 'Tạo voucher / campaign'), ('voucher.update', 'Sửa voucher', 'voucher', 'update', 'Cập nhật voucher / campaign'), ('voucher.delete', 'Xóa voucher', 'voucher', 'delete', 'Xóa voucher'), ('review.view', 'Xem review', 'review', 'view', 'Xem review'), ('review.reply', 'Phản hồi review', 'review', 'reply', 'Phản hồi đánh giá'), ('review.moderate', 'Kiểm duyệt review', 'review', 'moderate', 'Ẩn / duyệt review'), ('support.view', 'Xem support', 'support', 'view', 'Xem phiên hỗ trợ'), ('support.reply', 'Trả lời support', 'support', 'reply', 'Nhắn tin hỗ trợ khách hàng'), ('support.assign', 'Phân công support', 'support', 'assign', 'Gán staff xử lý hội thoại'), ('notification.view', 'Xem thông báo', 'notification', 'view', 'Xem thông báo hệ thống'), ('notification.send', 'Gửi thông báo', 'notification', 'send', 'Gửi push / email / sms'), ('report.view', 'Xem báo cáo', 'report', 'view', 'Xem thống kê / báo cáo'), ('audit.view', 'Xem audit log', 'audit', 'view', 'Xem lịch sử thao tác hệ thống'); 

INSERT IGNORE INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r JOIN permissions p WHERE r.code = 'SUPER_ADMIN'; 
INSERT IGNORE INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r JOIN permissions p WHERE r.code = 'ADMIN' AND p.code IN ('dashboard.view','user.view','user.create','user.update','user.delete','user.block','role.view','role.assign','permission.view','destination.view','destination.create','destination.update','destination.delete','destination.publish','content.view','content.create','content.update','content.delete','content.publish','tour.view','tour.create','tour.update','tour.delete','tour.publish','schedule.view','schedule.create','schedule.update','schedule.close','guide.assign','booking.view','booking.create','booking.update','booking.cancel','booking.checkin','payment.view','payment.create','payment.update','refund.view','refund.create','refund.approve','refund.reject','refund.process','voucher.view','voucher.create','voucher.update','voucher.delete','review.view','review.reply','review.moderate','support.view','support.reply','support.assign','notification.view','notification.send','report.view','audit.view'); 
INSERT IGNORE INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r JOIN permissions p WHERE r.code = 'CONTENT_EDITOR' AND p.code IN ('dashboard.view','destination.view','destination.create','destination.update','destination.publish','content.view','content.create','content.update','content.delete','content.publish','tour.view','tour.create','tour.update','tour.publish','schedule.view','review.view','notification.view'); 
INSERT IGNORE INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r JOIN permissions p WHERE r.code = 'FIELD_STAFF' AND p.code IN ('dashboard.view','destination.view','destination.create','destination.update','content.view','content.create','content.update','tour.view','schedule.view','schedule.update','booking.view','booking.checkin'); 
INSERT IGNORE INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r JOIN permissions p WHERE r.code = 'OPERATOR' AND p.code IN ('dashboard.view','user.view','destination.view','tour.view','schedule.view','schedule.create','schedule.update','schedule.close','guide.assign','booking.view','booking.create','booking.update','booking.cancel','booking.checkin','payment.view','payment.create','payment.update','refund.view','refund.create','refund.approve','refund.reject','refund.process','review.view','review.reply','support.view','support.reply','support.assign','notification.view','notification.send','report.view'); 
INSERT IGNORE INTO role_permissions (role_id, permission_id) SELECT r.id, p.id FROM roles r JOIN permissions p WHERE r.code = 'USER' AND p.code IN ('destination.view','content.view','tour.view','schedule.view','booking.view','booking.create','booking.cancel','payment.view','payment.create','refund.view','refund.create','review.view','notification.view','support.view','support.reply'); 

INSERT IGNORE INTO cancellation_policies ( id, name, description, voucher_bonus_percent, is_default, is_active ) 
VALUES (1, 'CHINH_SACH_MAC_DINH', 'Chính sách hoàn hủy mặc định của TravelViet', 10, TRUE, TRUE); 
INSERT IGNORE INTO cancellation_policy_rules ( id, policy_id, min_hours_before, max_hours_before, refund_percent, voucher_percent, fee_percent, allow_reschedule, notes ) 
VALUES (1, 1, 168, NULL, 80, 90, 20, TRUE, 'Hủy trước 7 ngày'), (2, 1, 72, 168, 50, 60, 50, TRUE, 'Hủy trước 3 ngày'), (3, 1, 0, 72, 0, 30, 100, FALSE, 'Hủy dưới 3 ngày'); 
INSERT IGNORE INTO tags(code, name, tag_group, description) 
VALUES ('GIAI_TRI', 'Giải trí', 'phong_cach', 'Tour vui chơi, hoạt động sôi động'), ('NGHI_DUONG', 'Nghỉ dưỡng', 'phong_cach', 'Tour nghỉ ngơi, thư giãn'), ('CHECKIN', 'Check-in', 'phong_cach', 'Tour phù hợp sống ảo, chụp hình'), ('GIA_DINH', 'Gia đình', 'doi_tuong', 'Tour phù hợp gia đình và trẻ em'), ('NGUOI_LON_TUOI', 'Người lớn tuổi', 'doi_tuong', 'Tour nhẹ nhàng, ít di chuyển'), ('SINH_VIEN', 'Sinh viên', 'doi_tuong', 'Tour giá tốt cho sinh viên'), ('BIEN', 'Biển', 'dia_hinh', 'Tour biển'), ('NUI', 'Núi', 'dia_hinh', 'Tour núi'), ('AM_THUC', 'Ẩm thực', 'trai_nghiem', 'Tour ẩm thực'), ('VAN_HOA', 'Văn hóa', 'trai_nghiem', 'Tour văn hóa');