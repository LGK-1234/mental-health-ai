-- AI 心理咨询会话相关表
-- 数据库：mental_health_assistant
-- 执行：mysql -uroot -p mental_health_assistant < sql/consultation.sql

CREATE TABLE IF NOT EXISTS consultation_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  session_title VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
  started_at DATETIME DEFAULT NULL COMMENT '开始时间',
  last_emotion_analysis TEXT DEFAULT NULL COMMENT '最后一次情绪分析结果(JSON)',
  last_emotion_updated_at DATETIME DEFAULT NULL COMMENT '情绪分析更新时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询会话';

CREATE TABLE IF NOT EXISTS consultation_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  session_id BIGINT NOT NULL COMMENT '会话ID',
  sender_type TINYINT NOT NULL COMMENT '1:用户 2:AI',
  message_type TINYINT NOT NULL COMMENT '1:文本',
  content TEXT NOT NULL COMMENT '消息内容',
  emotion_tag VARCHAR(50) DEFAULT NULL COMMENT '情绪标签',
  ai_model VARCHAR(50) DEFAULT NULL COMMENT 'AI模型',
  created_at DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询消息';
