-- GreenLoop Database Migration Script
-- Creates the users table and related indexes

-- Create database (if not exists)
CREATE DATABASE IF NOT EXISTS greenloop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE greenloop;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'User ID',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT 'User email address (unique)',
    name VARCHAR(255) NOT NULL COMMENT 'User full name',
    profile_picture_url VARCHAR(500) COMMENT 'URL to user profile picture from Google',
    role VARCHAR(50) NOT NULL DEFAULT 'consumer' COMMENT 'User role: consumer, retailer, dining_hall, donor, admin',    university_verified BOOLEAN DEFAULT FALSE COMMENT 'Verified as university user (.edu email)',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'User account active status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    last_login TIMESTAMP NULL COMMENT 'Last login timestamp',
    bio VARCHAR(500) COMMENT 'User bio/description',
    phone_number VARCHAR(20) COMMENT 'User phone number',
    address VARCHAR(255) COMMENT 'User street address',
    city VARCHAR(100) COMMENT 'User city',
    state VARCHAR(100) COMMENT 'User state/province',
    zip_code VARCHAR(10) COMMENT 'User postal code',
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active),
    INDEX idx_university_verified (university_verified),
    INDEX idx_city (city),
    INDEX idx_created_at (created_at),
    INDEX idx_last_login (last_login)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='GreenLoop user accounts and OAuth2 profiles';

-- Create refresh_tokens table (optional, for token management)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Token ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    token_hash VARCHAR(255) NOT NULL UNIQUE COMMENT 'Hashed refresh token',
    expires_at TIMESTAMP NOT NULL COMMENT 'Token expiration time',
    revoked BOOLEAN DEFAULT FALSE COMMENT 'Token revocation status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Token creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_revoked (revoked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Refresh token management for token rotation';

-- Create user_roles table (optional, for future RBAC enhancement)
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT COMMENT 'Admin user ID who assigned this role',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_user_role (user_id, role),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='User role assignments (for future multi-role support)';

-- Create audit_logs table (optional, for security auditing)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT 'User performing the action',
    action VARCHAR(100) NOT NULL COMMENT 'Action type (LOGIN, LOGOUT, UPDATE_PROFILE, etc)',
    resource_type VARCHAR(100) COMMENT 'Type of resource accessed',
    resource_id BIGINT COMMENT 'ID of resource accessed',
    details JSON COMMENT 'Additional details in JSON format',
    ip_address VARCHAR(45) COMMENT 'Client IP address',
    user_agent VARCHAR(500) COMMENT 'Client user agent',
    status VARCHAR(50) COMMENT 'Action status (SUCCESS, FAILURE)',
    error_message TEXT COMMENT 'Error message if action failed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Security audit log for all user actions';

-- Create listings table
CREATE TABLE IF NOT EXISTS listings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category ENUM('produce','dairy','bakery','prepared','beverage','pantry','frozen','other') NOT NULL,
    image_url VARCHAR(500),
    quantity INT NOT NULL,
    unit VARCHAR(30) NOT NULL,
    original_price DECIMAL(10,2),
    discounted_price DECIMAL(10,2),
    dietary_info VARCHAR(500),
    allergens VARCHAR(500),
    pickup_address VARCHAR(255),
    pickup_city VARCHAR(100),
    pickup_state VARCHAR(100),
    pickup_zip_code VARCHAR(20),
    pickup_latitude DOUBLE,
    pickup_longitude DOUBLE,
    pickup_window_start TIMESTAMP NOT NULL,
    pickup_window_end TIMESTAMP NOT NULL,
    status ENUM('available','reserved','collected','expired','cancelled') NOT NULL DEFAULT 'available',
    reservation_count INT DEFAULT 0,
    co2_saved_kg DECIMAL(10,2),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_owner_id (owner_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_expires_at (expires_at),
    INDEX idx_pickup_window_end (pickup_window_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Food listings created by donors/retailers';

-- Email verification tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BINARY(16) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_evt_user_id (user_id),
    INDEX idx_evt_token (token),
    INDEX idx_evt_expires_at (expires_at)
);

-- Create initial data (optional)
-- INSERT INTO users (email, name, role, university_verified, is_active)
-- VALUES ('admin@greenloop.com', 'Admin User', 'ADMIN', TRUE, TRUE);

-- Create sample views for common queries
CREATE OR REPLACE VIEW active_users_by_role AS
SELECT role, COUNT(*) as count
FROM users
WHERE is_active = TRUE
GROUP BY role;

CREATE OR REPLACE VIEW verified_universities AS
SELECT COUNT(*) as verified_count,
       (SELECT COUNT(*) FROM users) as total_count,
       ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM users), 2) as percentage
FROM users
WHERE university_verified = TRUE;

-- Add comments to tables
ALTER TABLE users COMMENT = 'GreenLoop user accounts';
ALTER TABLE refresh_tokens COMMENT = 'Refresh token management';
ALTER TABLE user_roles COMMENT = 'User role assignments';
ALTER TABLE audit_logs COMMENT = 'Security audit logs';

-- Set proper collation for string comparisons
ALTER TABLE users MODIFY email VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users MODIFY name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users MODIFY role VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'consumer';

-- Create stored procedures for common operations (optional)
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS GetUsersByRole(IN p_role VARCHAR(50))
BEGIN
    SELECT id, email, name, profile_picture_url, role, university_verified, is_active, created_at
    FROM users
    WHERE role = p_role AND is_active = TRUE
    ORDER BY created_at DESC;
END$$

CREATE PROCEDURE IF NOT EXISTS GetUserCount()
BEGIN
    SELECT role, COUNT(*) as count
    FROM users
    WHERE is_active = TRUE
    GROUP BY role;
END$$

CREATE PROCEDURE IF NOT EXISTS LogUserLogin(IN p_user_id BIGINT, IN p_ip_address VARCHAR(45))
BEGIN
    UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = p_user_id;

    INSERT INTO audit_logs (user_id, action, ip_address, status)
    VALUES (p_user_id, 'LOGIN', p_ip_address, 'SUCCESS');
END$$

DELIMITER ;

-- Verify tables were created
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'greenloop'
ORDER BY TABLE_NAME;

-- Sample queries for testing

-- Find a user by email
-- SELECT * FROM users WHERE email = 'test@university.edu';

-- Find all active consumers
-- SELECT * FROM users WHERE role = 'CONSUMER' AND is_active = TRUE;

-- Count users by role
-- SELECT role, COUNT(*) FROM users GROUP BY role;

-- Find recently created users
-- SELECT * FROM users ORDER BY created_at DESC LIMIT 10;

-- Find verified university users
-- SELECT * FROM users WHERE university_verified = TRUE AND is_active = TRUE;

-- Get user statistics
-- SELECT
--   COUNT(*) as total_users,
--   SUM(CASE WHEN role = 'CONSUMER' THEN 1 ELSE 0 END) as consumers,
--   SUM(CASE WHEN role = 'RETAILER' THEN 1 ELSE 0 END) as retailers,
--   SUM(CASE WHEN role = 'DINING_HALL' THEN 1 ELSE 0 END) as dining_halls,
--   SUM(CASE WHEN role = 'DONOR' THEN 1 ELSE 0 END) as donors,
--   SUM(CASE WHEN university_verified = TRUE THEN 1 ELSE 0 END) as verified_users
-- FROM users WHERE is_active = TRUE;
