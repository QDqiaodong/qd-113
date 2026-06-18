-- V4: 新增仓储适宜度账册

CREATE TABLE IF NOT EXISTS storage_suitability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tea_id BIGINT NOT NULL,
    tea_category VARCHAR(50) NOT NULL,
    storage_method VARCHAR(100),
    temperature DECIMAL(5, 2),
    humidity DECIMAL(5, 2),
    seal_condition VARCHAR(50),
    total_score INT,
    temperature_score INT,
    humidity_score INT,
    seal_score INT,
    storage_method_score INT,
    suitability_level VARCHAR(20),
    suggestions VARCHAR(1000),
    record_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_suitability_tea_id (tea_id),
    INDEX idx_suitability_record_date (record_date),
    INDEX idx_suitability_level (suitability_level),
    CONSTRAINT fk_suitability_tea FOREIGN KEY (tea_id) REFERENCES tea(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
