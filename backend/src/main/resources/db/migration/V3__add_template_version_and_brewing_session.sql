-- V3: 新增冲泡模板版本缓存表和单次冲泡记录表

CREATE TABLE IF NOT EXISTS template_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tea_category VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL,
    param_source VARCHAR(200),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_template_version_category (tea_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS brewing_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tea_id BIGINT NOT NULL,
    brewing_param_id BIGINT,
    actual_water_temperature INT,
    first_infusion_time INT,
    second_infusion_time INT,
    third_infusion_time INT,
    subsequent_infusion_time INT,
    taste_impression VARCHAR(1000),
    session_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_tea_id (tea_id),
    INDEX idx_session_date (session_date),
    CONSTRAINT fk_session_tea FOREIGN KEY (tea_id) REFERENCES tea(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO template_version (tea_category, version, param_source) VALUES
('绿茶', 2, '内置默认模板'),
('红茶', 2, '内置默认模板'),
('乌龙茶', 2, '内置默认模板'),
('普洱茶', 2, '内置默认模板'),
('白茶', 2, '内置默认模板'),
('黑茶', 2, '内置默认模板'),
('黄茶', 2, '内置默认模板'),
('花茶', 2, '内置默认模板');
