ALTER TABLE booking
ADD COLUMN deleted_at DATETIME DEFAULT NULL COMMENT '软删除时间' AFTER updated_at;

CREATE INDEX idx_deleted_at ON booking (deleted_at);
