-- WMS 库存失败操作重试及人工补偿记录
CREATE TABLE IF NOT EXISTS `wms_inventory_operation_retry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operation_type` varchar(32) NOT NULL COMMENT '操作：RESERVE/RELEASE/OUTBOUND/INBOUND_RETURN',
  `order_no` varchar(64) DEFAULT NULL COMMENT '商城订单号',
  `return_no` varchar(64) DEFAULT NULL COMMENT '售后单号',
  `payload` text NOT NULL COMMENT '库存操作明细 JSON',
  `status` tinyint NOT NULL COMMENT '0待重试 1处理中 2成功 3人工处理',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '自动重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次自动重试时间',
  `last_error` varchar(500) DEFAULT NULL COMMENT '最近一次错误',
  `creator` varchar(64) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_status_next_retry` (`status`, `next_retry_time`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS库存操作失败重试/人工补偿表';
