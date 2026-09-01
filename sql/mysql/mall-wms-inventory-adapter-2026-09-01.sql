-- 商城现货商品使用本地 WMS 表的库存预占记录。
-- 后续接入外部 WMS 时保留商城订单号和 SKU/仓库映射，只替换库存适配器实现。
CREATE TABLE IF NOT EXISTS `wms_inventory_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '商城履约订单号',
  `sku_id` bigint NOT NULL COMMENT 'WMS 物料 SKU 编号',
  `warehouse_id` bigint NOT NULL COMMENT 'WMS 仓库编号',
  `quantity` decimal(18,4) NOT NULL COMMENT '预占数量',
  `status` tinyint NOT NULL COMMENT '状态：0锁定 1已释放 2已出库',
  `creator` varchar(64) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sku_warehouse_deleted` (`order_no`, `sku_id`, `warehouse_id`, `deleted`),
  KEY `idx_sku_warehouse_status` (`sku_id`, `warehouse_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单 WMS 库存预占表';
