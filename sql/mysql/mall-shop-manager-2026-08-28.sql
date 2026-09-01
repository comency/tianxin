-- 产业链商城：企业店铺负责人绑定
-- 负责人为后台系统用户（system_users.id）。一个店铺当前绑定一名主负责人；
-- 历史订单不回填，新增订单会将当时负责人快照写入 trade_order.seller_user_id。

ALTER TABLE `product_shop`
    ADD COLUMN `manager_user_id` bigint DEFAULT NULL COMMENT '店铺负责人后台用户编号' AFTER `contact_mobile`,
    ADD KEY `idx_product_shop_manager_user` (`manager_user_id`);
