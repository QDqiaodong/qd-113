-- V1: 初始化数据库 schema

CREATE TABLE IF NOT EXISTS tea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    tea_category VARCHAR(50) NOT NULL,
    origin_region VARCHAR(100) NOT NULL,
    harvest_year INT,
    storage_method VARCHAR(100),
    current_stock DECIMAL(10, 2),
    stock_unit VARCHAR(20),
    description VARCHAR(500),
    image_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tea_category (tea_category),
    INDEX idx_origin_region (origin_region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS brewing_param (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tea_id BIGINT NOT NULL,
    param_name VARCHAR(100),
    water_temperature INT NOT NULL,
    tea_amount DECIMAL(5, 2) NOT NULL,
    tea_ratio VARCHAR(50),
    water_amount DECIMAL(6, 2),
    first_infusion_time INT NOT NULL,
    second_infusion_time INT,
    third_infusion_time INT,
    subsequent_infusion_time INT,
    total_infusions INT,
    water_quality VARCHAR(50),
    notes VARCHAR(500),
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brewing_tea_id (tea_id),
    CONSTRAINT fk_brewing_tea FOREIGN KEY (tea_id) REFERENCES tea(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS storage_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tea_id BIGINT NOT NULL,
    storage_location VARCHAR(100),
    temperature DECIMAL(5, 2),
    humidity DECIMAL(5, 2),
    seal_condition VARCHAR(50),
    stock_change DECIMAL(10, 2),
    current_stock DECIMAL(10, 2),
    record_date DATETIME,
    notes VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_storage_tea_id (tea_id),
    INDEX idx_storage_record_date (record_date),
    CONSTRAINT fk_storage_tea FOREIGN KEY (tea_id) REFERENCES tea(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tasting_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tea_id BIGINT NOT NULL,
    brewing_method VARCHAR(100),
    aroma_score INT,
    aroma_desc VARCHAR(500),
    liquor_color_score INT,
    liquor_color_desc VARCHAR(500),
    taste_score INT,
    taste_desc VARCHAR(500),
    aftertaste_score INT,
    aftertaste_desc VARCHAR(500),
    overall_score INT,
    impression VARCHAR(1000),
    tasting_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tasting_tea_id (tea_id),
    INDEX idx_tasting_date (tasting_date),
    CONSTRAINT fk_tasting_tea FOREIGN KEY (tea_id) REFERENCES tea(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
