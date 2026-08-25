CREATE DATABASE IF NOT EXISTS tianxin_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE tianxin_platform;

CREATE TABLE IF NOT EXISTS sys_user (
  id CHAR(36) NOT NULL,
  username VARCHAR(32) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  password_hash VARCHAR(256) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
  role_code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  permissions VARCHAR(2000) NOT NULL,
  PRIMARY KEY (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id CHAR(36) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  PRIMARY KEY (user_id, role_code),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_code) REFERENCES sys_role(role_code) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系';

CREATE TABLE IF NOT EXISTS enterprise_company (
  id CHAR(36) NOT NULL,
  company_code VARCHAR(64) NOT NULL,
  company_name VARCHAR(128) NOT NULL,
  category VARCHAR(32) NOT NULL,
  category_name VARCHAR(32) NOT NULL,
  business_scope VARCHAR(500) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_enterprise_company_code (company_code),
  KEY idx_enterprise_company_category (category, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业名录';

INSERT INTO enterprise_company (id, company_code, company_name, category, category_name, business_scope, sort_order, enabled) VALUES
(UUID(), 'UPSTREAM_XIBEI_HENGTAI', '西北恒泰新材料有限公司', 'UPSTREAM', '原材料供应商', '主营岩棉、玻璃棉保温基材，工业保温棉坯料供应', 1, 1),
(UUID(), 'UPSTREAM_JINCHENG_RESIN', '锦程树脂化工科技有限公司', 'UPSTREAM', '原材料供应商', '聚氨酯组合料、环氧树脂、防腐树脂原料生产', 2, 1),
(UUID(), 'UPSTREAM_LANDUN_COATING', '蓝盾涂料原料集团', 'UPSTREAM', '原材料供应商', '防腐涂料基料、固化剂、防锈粉体、颜填料供应', 3, 1),
(UUID(), 'UPSTREAM_LONGDA_RUBBER', '隆达橡塑材料股份有限公司', 'UPSTREAM', '原材料供应商', '橡塑海绵原料、丁基胶、密封胶带基材', 4, 1),
(UUID(), 'UPSTREAM_JINYAN_MINERAL_WOOL', '金岩矿物棉实业有限公司', 'UPSTREAM', '原材料供应商', '玄武岩岩棉原料、矿渣棉原材料加工供货', 5, 1),
(UUID(), 'UPSTREAM_HUITAI_FIBERGLASS', '汇泰玻纤科技有限公司', 'UPSTREAM', '原材料供应商', '玻璃纤维布、网格布、防腐增强玻纤基材', 6, 1),
(UUID(), 'UPSTREAM_YUANG_CHEMICAL', '宇昂化学助剂有限公司', 'UPSTREAM', '原材料供应商', '防腐固化剂、阻燃剂、防水助剂、发泡剂', 7, 1),
(UUID(), 'MANUFACTURER_SHAANAN', '陕安防腐保温制品有限公司', 'MANUFACTURER', '生产制造企业', '预制直埋保温管、聚氨酯保温板、管道保温构件', 1, 1),
(UUID(), 'MANUFACTURER_KEDUN', '科盾防护科技股份有限公司', 'MANUFACTURER', '生产制造企业', '工业防腐涂料、环氧重防腐漆、耐高温防腐涂层成品', 2, 1),
(UUID(), 'MANUFACTURER_HUAHAN', '华瀚节能建材有限公司', 'MANUFACTURER', '生产制造企业', '岩棉板、橡塑保温板、硅酸铝保温制品成套生产', 3, 1),
(UUID(), 'MANUFACTURER_RUIJIE_PIPE', '锐捷管道防护设备有限公司', 'MANUFACTURER', '生产制造企业', '钢套钢保温管、弯头三通预制保温管件', 4, 1),
(UUID(), 'MANUFACTURER_GUBANG', '固邦防腐材料制造有限公司', 'MANUFACTURER', '生产制造企业', '防腐卷材、环氧胶泥、防腐腻子、地坪防腐材料', 5, 1),
(UUID(), 'MANUFACTURER_ZHUOYUAN', '卓远耐火保温实业公司', 'MANUFACTURER', '生产制造企业', '耐高温硅酸铝模块、锅炉专用保温成品件', 6, 1),
(UUID(), 'CONSTRUCTION_SHENGAN', '盛安工业防腐安装工程有限公司', 'CONSTRUCTION', '施工安装企业', '化工设备、储罐、管道防腐保温总承包施工', 1, 1),
(UUID(), 'CONSTRUCTION_XINYUAN', '鑫源节能建设工程有限公司', 'CONSTRUCTION', '施工安装企业', '热力管网预制管敷设、现场保温外包施工', 2, 1),
(UUID(), 'CONSTRUCTION_HENGXIN', '恒信防护工程技术有限公司', 'CONSTRUCTION', '施工安装企业', '钢结构防腐喷涂、储罐内壁防腐、炉体保温检修', 3, 1),
(UUID(), 'CONSTRUCTION_TUOYUAN', '拓远工业维保安装公司', 'CONSTRUCTION', '施工安装企业', '电厂、化工厂防腐保温技改、大修项目现场实施', 4, 1),
(UUID(), 'CONSTRUCTION_ANKE', '安科防腐工程有限公司', 'CONSTRUCTION', '施工安装企业', '油气设备、储罐防腐喷砂除锈 + 保温一体化施工', 5, 1),
(UUID(), 'OWNER_YANHE_HEATING', '延河热力集团有限公司', 'OWNER', '工程甲方', '城市集中供热管网改造项目甲方', 1, 1),
(UUID(), 'OWNER_QINCHUAN_CHEMICAL', '秦川化工产业园发展有限公司', 'OWNER', '工程甲方', '化工园区装置设备防腐保温采购甲方', 2, 1),
(UUID(), 'OWNER_LONGYUAN_ENERGY', '陇原能源发电有限责任公司', 'OWNER', '工程甲方', '热电厂锅炉、管道防腐保温工程建设方', 3, 1),
(UUID(), 'OWNER_NORTHWEST_URBAN', '西北城投基建发展集团', 'OWNER', '工程甲方', '市政热力、工业园区基建项目业主单位', 4, 1),
(UUID(), 'OWNER_HANHAI_PETROCHEMICAL', '瀚海石化实业有限公司', 'OWNER', '工程甲方', '石化储罐、工艺管线防腐保温项目甲方', 5, 1),
(UUID(), 'OWNER_SHANBEI_MINING', '陕北工矿集团有限责任公司', 'OWNER', '工程甲方', '工矿厂区设备保温防腐技改业主', 6, 1)
ON DUPLICATE KEY UPDATE
  company_name = VALUES(company_name),
  category = VALUES(category),
  category_name = VALUES(category_name),
  business_scope = VALUES(business_scope),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);
