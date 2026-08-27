-- 将产业链中可供货的上游、生产制造企业同步为商城企业店铺。
-- 施工安装企业与工程甲方属于服务/需求方，保留在 CRM 与工程项目流程，不创建商品店铺。
INSERT INTO `product_shop` (
  `enterprise_id`, `name`, `introduction`, `status`, `tenant_id`,
  `creator`, `updater`, `deleted`
)
SELECT
  e.`id`, e.`name`, CONCAT(e.`category_name`, '｜', e.`business_scope`),
  0, 1, '1', '1', b'0'
FROM `tianxin_enterprise` e
LEFT JOIN `product_shop` s
  ON s.`enterprise_id` = e.`id` AND s.`deleted` = b'0'
WHERE e.`deleted` = b'0'
  AND e.`status` = 0
  AND e.`category` IN ('UPSTREAM', 'MANUFACTURER')
  AND s.`id` IS NULL;
