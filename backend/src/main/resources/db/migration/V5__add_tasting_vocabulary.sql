-- V5: 新增品鉴词汇预设库

CREATE TABLE IF NOT EXISTS tasting_vocabulary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vocabulary_type VARCHAR(20) NOT NULL COMMENT '词汇类型: aroma香气, liquor_color汤色, taste滋味, aftertaste回甘',
    tea_category VARCHAR(50) NOT NULL COMMENT '适用茶类',
    word VARCHAR(50) NOT NULL COMMENT '词汇',
    description VARCHAR(200) COMMENT '词汇描述/解释',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vocabulary_type (vocabulary_type),
    INDEX idx_tea_category (tea_category),
    INDEX idx_type_category (vocabulary_type, tea_category),
    UNIQUE KEY uk_type_category_word (vocabulary_type, tea_category, word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品鉴词汇预设库';
