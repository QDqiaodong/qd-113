-- V2: 为冲泡参数表添加冲泡方法字段

ALTER TABLE brewing_param
ADD COLUMN brewing_method VARCHAR(100) AFTER param_name;

-- 为已有的数据设置默认冲泡方法
UPDATE brewing_param SET brewing_method = '盖碗冲泡' WHERE brewing_method IS NULL;

-- 添加索引
CREATE INDEX idx_brewing_tea_method ON brewing_param (tea_id, brewing_method);
