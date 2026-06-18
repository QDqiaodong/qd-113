-- V6: 新增冲泡会话库存扣减字段

ALTER TABLE brewing_session
    ADD COLUMN stock_deducted TINYINT(1) DEFAULT 0 COMMENT '是否已扣减库存' AFTER taste_impression,
    ADD COLUMN stock_amount DECIMAL(10,2) DEFAULT NULL COMMENT '扣减的茶叶数量' AFTER stock_deducted,
    ADD COLUMN storage_record_id BIGINT DEFAULT NULL COMMENT '关联的仓储变动记录ID' AFTER stock_amount,
    ADD INDEX idx_storage_record_id (storage_record_id);

ALTER TABLE storage_record
    ADD COLUMN brewing_session_id BIGINT DEFAULT NULL COMMENT '关联的冲泡会话ID' AFTER seal_condition,
    ADD INDEX idx_brewing_session_id (brewing_session_id);
