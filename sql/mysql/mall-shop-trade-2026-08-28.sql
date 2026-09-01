-- 产业链商城：店铺化交易基础
-- 执行前请先在测试库备份 trade_order、trade_order_item、trade_after_sale。
-- 历史订单默认归入“平台自营”（shop_id = 0），不根据当前商品主数据反推历史快照。

ALTER TABLE `trade_order`
    ADD COLUMN `shop_id` bigint NOT NULL DEFAULT 0 COMMENT '履约店铺编号；0 表示平台自营' AFTER `id`,
    ADD COLUMN `shop_name` varchar(64) NOT NULL DEFAULT '' COMMENT '下单时店铺名称快照' AFTER `shop_id`,
    ADD COLUMN `shop_logo_url` varchar(512) DEFAULT NULL COMMENT '下单时店铺 Logo 快照' AFTER `shop_name`,
    ADD COLUMN `seller_user_id` bigint DEFAULT NULL COMMENT '商家订单处理人编号' AFTER `shop_logo_url`,
    ADD KEY `idx_trade_order_shop_status_time` (`shop_id`, `status`, `create_time`),
    ADD KEY `idx_trade_order_seller_user` (`seller_user_id`);

ALTER TABLE `trade_order_item`
    ADD COLUMN `shop_id` bigint NOT NULL DEFAULT 0 COMMENT '履约店铺编号；冗余订单店铺归属' AFTER `order_id`,
    ADD KEY `idx_trade_order_item_shop` (`shop_id`);

ALTER TABLE `trade_after_sale`
    ADD COLUMN `shop_id` bigint NOT NULL DEFAULT 0 COMMENT '售后责任店铺编号；0 表示平台自营' AFTER `user_id`,
    ADD COLUMN `handler_user_id` bigint DEFAULT NULL COMMENT '商家或平台售后处理人编号' AFTER `shop_id`,
    ADD COLUMN `handle_reason` varchar(512) DEFAULT NULL COMMENT '审核、拒绝或退款说明' AFTER `handler_user_id`,
    ADD KEY `idx_trade_after_sale_shop_status` (`shop_id`, `status`),
    ADD KEY `idx_trade_after_sale_handler_user` (`handler_user_id`);
