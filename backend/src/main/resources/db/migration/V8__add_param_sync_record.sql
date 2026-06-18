-- V8: 添加参数同步记录表

CREATE TABLE IF NOT EXISTS param_sync_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_tea_id BIGINT NOT NULL,
    source_tea_name VARCHAR(100) NOT NULL,
    source_param_id BIGINT NOT NULL,
    source_param_name VARCHAR(100),
    target_tea_id BIGINT NOT NULL,
    target_tea_name VARCHAR(100) NOT NULL,
    target_param_id BIGINT,
    target_param_name VARCHAR(100),
    tea_category VARCHAR(50) NOT NULL,
    field_differences TEXT,
    sync_type VARCHAR(20) NOT NULL DEFAULT 'DEFAULT',
    synced_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source_tea_id (source_tea_id),
    INDEX idx_target_tea_id (target_tea_id),
    INDEX idx_tea_category (tea_category),
    INDEX idx_synced_at (synced_at),
    CONSTRAINT fk_param_sync_source_tea FOREIGN KEY (source_tea_id) REFERENCES tea(id) ON DELETE CASCADE,
    CONSTRAINT fk_param_sync_target_tea FOREIGN KEY (target_tea_id) REFERENCES tea(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
