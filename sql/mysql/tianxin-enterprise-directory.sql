-- 天信之星防腐保温智慧平台：企业名录初始化数据
-- 执行前请先初始化 ruoyi-vue-pro 基础库与各业务模块。

CREATE TABLE IF NOT EXISTS `tianxin_enterprise` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `category` varchar(32) NOT NULL COMMENT '企业角色编码：UPSTREAM、MANUFACTURER、CONSTRUCTION、OWNER',
  `category_name` varchar(32) NOT NULL COMMENT '企业角色名称',
  `name` varchar(128) NOT NULL COMMENT '企业名称',
  `business_scope` varchar(500) NOT NULL COMMENT '主营产品或服务',
  `sort` int NOT NULL DEFAULT 0 COMMENT '展示排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0启用 1停用',
  `creator` varchar(64) DEFAULT '1' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '1' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `deleted_time` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tianxin_enterprise_name` (`name`),
  KEY `idx_tianxin_enterprise_category` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='天信产业链企业名录';

INSERT INTO `tianxin_enterprise` (`category`, `category_name`, `name`, `business_scope`, `sort`) VALUES
('UPSTREAM', '原材料供应商', '西北恒泰新材料有限公司', '岩棉、玻璃棉保温基材，工业保温棉坯料供应', 1),
('UPSTREAM', '原材料供应商', '锦程树脂化工科技有限公司', '聚氨酯组合料、环氧树脂、防腐树脂原料生产', 2),
('UPSTREAM', '原材料供应商', '蓝盾涂料原料集团', '防腐涂料基料、固化剂、防锈粉体、颜填料供应', 3),
('UPSTREAM', '原材料供应商', '隆达橡塑材料股份有限公司', '橡塑海绵原料、丁基胶、密封胶带基材', 4),
('UPSTREAM', '原材料供应商', '金岩矿物棉实业有限公司', '玄武岩岩棉原料、矿渣棉原材料加工供货', 5),
('UPSTREAM', '原材料供应商', '汇泰玻纤科技有限公司', '玻璃纤维布、网格布、防腐增强玻纤基材', 6),
('UPSTREAM', '原材料供应商', '宇昂化学助剂有限公司', '防腐固化剂、阻燃剂、防水助剂、发泡剂', 7),
('MANUFACTURER', '生产制造企业', '陕安防腐保温制品有限公司', '预制直埋保温管、聚氨酯保温板、管道保温构件', 1),
('MANUFACTURER', '生产制造企业', '科盾防护科技股份有限公司', '工业防腐涂料、环氧重防腐漆、耐高温防腐涂层成品', 2),
('MANUFACTURER', '生产制造企业', '华瀚节能建材有限公司', '岩棉板、橡塑保温板、硅酸铝保温制品成套生产', 3),
('MANUFACTURER', '生产制造企业', '锐捷管道防护设备有限公司', '钢套钢保温管、弯头三通预制保温管件', 4),
('MANUFACTURER', '生产制造企业', '固邦防腐材料制造有限公司', '防腐卷材、环氧胶泥、防腐腻子、地坪防腐材料', 5),
('MANUFACTURER', '生产制造企业', '卓远耐火保温实业公司', '耐高温硅酸铝模块、锅炉专用保温成品件', 6),
('CONSTRUCTION', '施工安装企业', '盛安工业防腐安装工程有限公司', '化工设备、储罐、管道防腐保温总承包施工', 1),
('CONSTRUCTION', '施工安装企业', '鑫源节能建设工程有限公司', '热力管网预制管敷设、现场保温外包施工', 2),
('CONSTRUCTION', '施工安装企业', '恒信防护工程技术有限公司', '钢结构防腐喷涂、储罐内壁防腐、炉体保温检修', 3),
('CONSTRUCTION', '施工安装企业', '拓远工业维保安装公司', '电厂、化工厂防腐保温技改、大修项目现场实施', 4),
('CONSTRUCTION', '施工安装企业', '安科防腐工程有限公司', '油气设备、储罐防腐喷砂除锈与保温一体化施工', 5),
('OWNER', '工程甲方', '延河热力集团有限公司', '城市集中供热管网改造项目甲方', 1),
('OWNER', '工程甲方', '秦川化工产业园发展有限公司', '化工园区装置设备防腐保温采购甲方', 2),
('OWNER', '工程甲方', '陇原能源发电有限责任公司', '热电厂锅炉、管道防腐保温工程建设方', 3),
('OWNER', '工程甲方', '西北城投基建发展集团', '市政热力、工业园区基建项目业主单位', 4),
('OWNER', '工程甲方', '瀚海石化实业有限公司', '石化储罐、工艺管线防腐保温项目甲方', 5),
('OWNER', '工程甲方', '陕北工矿集团有限责任公司', '工矿厂区设备保温防腐技改业主', 6)
ON DUPLICATE KEY UPDATE
  `category` = VALUES(`category`),
  `category_name` = VALUES(`category_name`),
  `business_scope` = VALUES(`business_scope`),
  `sort` = VALUES(`sort`),
  `status` = 0,
  `updater` = '1',
  `update_time` = CURRENT_TIMESTAMP,
  `deleted` = b'0',
  `deleted_time` = NULL;

-- 同步到新基座已有的 ERP 供应商页面：上游原料商与生产制造商。
INSERT INTO `erp_supplier` (`name`, `remark`, `status`, `sort`, `creator`, `updater`, `deleted`, `tenant_id`)
SELECT `seed`.`name`, CONCAT('天信企业名录｜', `seed`.`category_name`, '｜', `seed`.`business_scope`), 0,
       `seed`.`sort`, '1', '1', b'0', 1
FROM (
  SELECT '原材料供应商' AS `category_name`, '西北恒泰新材料有限公司' AS `name`, '岩棉、玻璃棉保温基材，工业保温棉坯料供应' AS `business_scope`, 1 AS `sort` UNION ALL
  SELECT '原材料供应商', '锦程树脂化工科技有限公司', '聚氨酯组合料、环氧树脂、防腐树脂原料生产', 2 UNION ALL
  SELECT '原材料供应商', '蓝盾涂料原料集团', '防腐涂料基料、固化剂、防锈粉体、颜填料供应', 3 UNION ALL
  SELECT '原材料供应商', '隆达橡塑材料股份有限公司', '橡塑海绵原料、丁基胶、密封胶带基材', 4 UNION ALL
  SELECT '原材料供应商', '金岩矿物棉实业有限公司', '玄武岩岩棉原料、矿渣棉原材料加工供货', 5 UNION ALL
  SELECT '原材料供应商', '汇泰玻纤科技有限公司', '玻璃纤维布、网格布、防腐增强玻纤基材', 6 UNION ALL
  SELECT '原材料供应商', '宇昂化学助剂有限公司', '防腐固化剂、阻燃剂、防水助剂、发泡剂', 7 UNION ALL
  SELECT '生产制造企业', '陕安防腐保温制品有限公司', '预制直埋保温管、聚氨酯保温板、管道保温构件', 11 UNION ALL
  SELECT '生产制造企业', '科盾防护科技股份有限公司', '工业防腐涂料、环氧重防腐漆、耐高温防腐涂层成品', 12 UNION ALL
  SELECT '生产制造企业', '华瀚节能建材有限公司', '岩棉板、橡塑保温板、硅酸铝保温制品成套生产', 13 UNION ALL
  SELECT '生产制造企业', '锐捷管道防护设备有限公司', '钢套钢保温管、弯头三通预制保温管件', 14 UNION ALL
  SELECT '生产制造企业', '固邦防腐材料制造有限公司', '防腐卷材、环氧胶泥、防腐腻子、地坪防腐材料', 15 UNION ALL
  SELECT '生产制造企业', '卓远耐火保温实业公司', '耐高温硅酸铝模块、锅炉专用保温成品件', 16
) AS `seed`
LEFT JOIN `erp_supplier` AS `existing` ON `existing`.`name` = `seed`.`name` AND `existing`.`deleted` = b'0'
WHERE `existing`.`id` IS NULL;

-- 同步到新基座已有的 CRM 客户页面：施工服务商与工程甲方。
INSERT INTO `crm_customer` (`name`, `follow_up_status`, `owner_time`, `lock_status`, `deal_status`, `remark`, `creator`, `updater`, `deleted`, `tenant_id`)
SELECT `seed`.`name`, 0, NOW(), b'0', b'0',
       CONCAT('天信企业名录｜', `seed`.`category_name`, '｜', `seed`.`business_scope`), '1', '1', b'0', 1
FROM (
  SELECT '施工安装企业' AS `category_name`, '盛安工业防腐安装工程有限公司' AS `name`, '化工设备、储罐、管道防腐保温总承包施工' AS `business_scope` UNION ALL
  SELECT '施工安装企业', '鑫源节能建设工程有限公司', '热力管网预制管敷设、现场保温外包施工' UNION ALL
  SELECT '施工安装企业', '恒信防护工程技术有限公司', '钢结构防腐喷涂、储罐内壁防腐、炉体保温检修' UNION ALL
  SELECT '施工安装企业', '拓远工业维保安装公司', '电厂、化工厂防腐保温技改、大修项目现场实施' UNION ALL
  SELECT '施工安装企业', '安科防腐工程有限公司', '油气设备、储罐防腐喷砂除锈与保温一体化施工' UNION ALL
  SELECT '工程甲方', '延河热力集团有限公司', '城市集中供热管网改造项目甲方' UNION ALL
  SELECT '工程甲方', '秦川化工产业园发展有限公司', '化工园区装置设备防腐保温采购甲方' UNION ALL
  SELECT '工程甲方', '陇原能源发电有限责任公司', '热电厂锅炉、管道防腐保温工程建设方' UNION ALL
  SELECT '工程甲方', '西北城投基建发展集团', '市政热力、工业园区基建项目业主单位' UNION ALL
  SELECT '工程甲方', '瀚海石化实业有限公司', '石化储罐、工艺管线防腐保温项目甲方' UNION ALL
  SELECT '工程甲方', '陕北工矿集团有限责任公司', '工矿厂区设备保温防腐技改业主'
) AS `seed`
LEFT JOIN `crm_customer` AS `existing` ON `existing`.`name` = `seed`.`name` AND `existing`.`deleted` = b'0'
WHERE `existing`.`id` IS NULL;
