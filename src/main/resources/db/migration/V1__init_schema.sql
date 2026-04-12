-- V1__init_schema.sql
-- Initial schema for TravelViet Booking System

-- 1. Users module
CREATE TABLE users (
    id CHAR(36) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    full_name VARCHAR(150) NOT NULL,
    display_name VARCHAR(120),
    gender VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    date_of_birth DATE,
    avatar_url TEXT,
    member_level VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    loyalty_points INT NOT NULL DEFAULT 0,
    total_spent DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    email_verified_at DATETIME,
    phone_verified_at DATETIME,
    last_login_at DATETIME,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Destinations module
CREATE TABLE destinations (
    id BIGINT AUTO_INCREMENT NOT NULL,
    uuid CHAR(36) NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    country_code VARCHAR(2) NOT NULL DEFAULT 'VN',
    province VARCHAR(120) NOT NULL,
    district VARCHAR(120),
    region VARCHAR(120),
    address TEXT,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    short_description TEXT,
    description TEXT,
    best_time_from_month INT,
    best_time_to_month INT,
    crowd_level_default VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    proposed_by CHAR(36),
    verified_by CHAR(36),
    rejection_reason TEXT,
    is_official BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_destinations_uuid (uuid),
    UNIQUE KEY uk_destinations_code (code),
    UNIQUE KEY uk_destinations_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_media (
    id BIGINT AUTO_INCREMENT NOT NULL,
    destination_id BIGINT NOT NULL,
    media_type VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
    media_url TEXT NOT NULL,
    alt_text VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_media_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_foods (
    id BIGINT AUTO_INCREMENT NOT NULL,
    destination_id BIGINT NOT NULL,
    food_name VARCHAR(200) NOT NULL,
    description TEXT,
    is_featured BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_foods_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_specialties (
    id BIGINT AUTO_INCREMENT NOT NULL,
    destination_id BIGINT NOT NULL,
    specialty_name VARCHAR(200) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_specialties_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_activities (
    id BIGINT AUTO_INCREMENT NOT NULL,
    destination_id BIGINT NOT NULL,
    activity_name VARCHAR(200) NOT NULL,
    description TEXT,
    activity_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_activities_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_tips (
    id BIGINT AUTO_INCREMENT NOT NULL,
    destination_id BIGINT NOT NULL,
    tip_title VARCHAR(200) NOT NULL,
    tip_content TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tips_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_events (
    id BIGINT AUTO_INCREMENT NOT NULL,
    destination_id BIGINT NOT NULL,
    event_name VARCHAR(200) NOT NULL,
    event_type VARCHAR(80),
    description TEXT,
    starts_at DATETIME,
    ends_at DATETIME,
    notify_all_followers BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_events_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE destination_follows (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id CHAR(36) NOT NULL,
    destination_id BIGINT NOT NULL,
    notify_event BOOLEAN NOT NULL DEFAULT TRUE,
    notify_voucher BOOLEAN NOT NULL DEFAULT TRUE,
    notify_new_tour BOOLEAN NOT NULL DEFAULT TRUE,
    notify_best_season BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_destination_follow (user_id, destination_id),
    CONSTRAINT fk_follows_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tours module
CREATE TABLE tours (
    id BIGINT AUTO_INCREMENT NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(280) NOT NULL,
    destination_id BIGINT NOT NULL,
    cancellation_policy_id BIGINT,
    short_description TEXT,
    description TEXT,
    highlights TEXT,
    inclusions TEXT,
    exclusions TEXT,
    notes TEXT,
    duration_days INT NOT NULL,
    duration_nights INT NOT NULL DEFAULT 0,
    base_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    transport_type VARCHAR(120),
    trip_mode VARCHAR(50),
    difficulty_level INT NOT NULL DEFAULT 1,
    activity_level INT NOT NULL DEFAULT 1,
    min_age INT,
    max_age INT,
    min_group_size INT NOT NULL DEFAULT 1,
    max_group_size INT NOT NULL DEFAULT 50,
    is_student_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    is_family_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    is_senior_friendly BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    total_reviews INT NOT NULL DEFAULT 0,
    total_bookings INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    created_by CHAR(36),
    updated_by CHAR(36),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tours_code (code),
    UNIQUE KEY uk_tours_slug (slug),
    CONSTRAINT fk_tours_destination FOREIGN KEY (destination_id) REFERENCES destinations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tour_schedules (
    id BIGINT AUTO_INCREMENT NOT NULL,
    schedule_code VARCHAR(40) NOT NULL,
    tour_id BIGINT NOT NULL,
    departure_at DATETIME NOT NULL,
    return_at DATETIME NOT NULL,
    booking_open_at DATETIME,
    booking_close_at DATETIME,
    meeting_at DATETIME,
    meeting_point_name VARCHAR(200),
    meeting_address TEXT,
    meeting_latitude DECIMAL(10,7),
    meeting_longitude DECIMAL(10,7),
    capacity_total INT NOT NULL,
    booked_seats INT NOT NULL DEFAULT 0,
    min_guests_to_operate INT NOT NULL DEFAULT 1,
    adult_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    child_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    infant_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    senior_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    single_room_surcharge DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    transport_detail VARCHAR(255),
    note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedules_code (schedule_code),
    CONSTRAINT fk_schedules_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bookings module
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT NOT NULL,
    booking_code VARCHAR(50) NOT NULL,
    user_id CHAR(36) NOT NULL,
    tour_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'pending_payment',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'unpaid',
    contact_name VARCHAR(150) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_email VARCHAR(150),
    adults INT NOT NULL DEFAULT 1,
    children INT NOT NULL DEFAULT 0,
    infants INT NOT NULL DEFAULT 0,
    seniors INT NOT NULL DEFAULT 0,
    subtotal_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookings_code (booking_code),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_tour FOREIGN KEY (tour_id) REFERENCES tours(id),
    CONSTRAINT fk_bookings_schedule FOREIGN KEY (schedule_id) REFERENCES tour_schedules(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE booking_passengers (
    id BIGINT AUTO_INCREMENT NOT NULL,
    booking_id BIGINT NOT NULL,
    passenger_type VARCHAR(20) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    date_of_birth DATE,
    identity_no VARCHAR(50),
    passport_no VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(150),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_passengers_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Payments module
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT NOT NULL,
    payment_code VARCHAR(50) NOT NULL,
    booking_id BIGINT NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    provider VARCHAR(100),
    transaction_ref VARCHAR(150),
    amount DECIMAL(14,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status VARCHAR(30) NOT NULL,
    paid_at DATETIME,
    request_payload JSON,
    response_payload JSON,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_code (payment_code),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
