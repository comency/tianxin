-- 防腐保温产业商城：首批可售商品演示数据
-- 依赖：mall-shop-enterprise-seed-2026-08-26.sql 已执行，且 OSS 已存在一张可访问图片。

SET NAMES utf8mb4;
SET @img := 'https://java-ai-comency.oss-cn-beijing.aliyuncs.com/20260827/Quicker_20251224_180451.png';

INSERT INTO product_category (id, parent_id, name, pic_url, sort, status, creator, tenant_id, deleted)
VALUES
  (20000, 0, '保温防腐材料', @img, 1, 0, '1', 1, b'0'),
  (20001, 20000, '保温基材', @img, 1, 0, '1', 1, b'0'),
  (20002, 20000, '防腐涂料', @img, 2, 0, '1', 1, b'0'),
  (20003, 20000, '密封辅材', @img, 3, 0, '1', 1, b'0'),
  (20004, 20000, '工程服务', @img, 4, 0, '1', 1, b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), pic_url = VALUES(pic_url), sort = VALUES(sort), status = VALUES(status), deleted = b'0';

INSERT INTO product_spu
  (id, shop_id, name, keyword, introduction, description, category_id, pic_url, slider_pic_urls, sort, status,
   spec_type, price, market_price, cost_price, stock, delivery_types, delivery_template_id, give_integral,
   sales_count, virtual_sales_count, browse_count, creator, tenant_id, deleted)
VALUES
  (20001, 1, '工业岩棉保温板 50mm', '岩棉板,工业保温,保温基材', '适用于设备、管道与建筑外墙的工业级岩棉保温板。',
   '<p>工业级岩棉保温板，适用于管道、储罐及设备保温场景。</p>', 20001, @img, JSON_ARRAY(@img), 1, 1,
   b'0', 4800, 5600, 3600, 500, '1', 1, 0, 0, 0, 0, '1', 1, b'0'),
  (20002, 2, '聚氨酯保温组合料 A/B 组', '聚氨酯,组合料,发泡保温', '双组份聚氨酯组合料，适用于预制直埋管及保温板生产。',
   '<p>聚氨酯双组份组合料，供预制保温管和保温板生产使用。</p>', 20001, @img, JSON_ARRAY(@img), 2, 1,
   b'0', 16800, 18800, 13500, 300, '1', 1, 0, 0, 0, 0, '1', 1, b'0'),
  (20003, 3, '环氧重防腐面漆 20kg', '环氧漆,防腐涂料,重防腐', '适用于钢结构、储罐和工艺管线的环氧重防腐涂装。',
   '<p>环氧重防腐面漆，适用于工业设备、钢结构与储罐表面防护。</p>', 20002, @img, JSON_ARRAY(@img), 3, 1,
   b'0', 68000, 76000, 51000, 200, '1', 1, 0, 0, 0, 0, '1', 1, b'0'),
  (20004, 4, 'B1级橡塑保温板 20mm', '橡塑保温,阻燃,保温板', '柔性橡塑保温板，适用于空调管道、设备及冷冻水系统。',
   '<p>B1 级橡塑保温板，具有良好的保温、防结露和阻燃性能。</p>', 20001, @img, JSON_ARRAY(@img), 4, 1,
   b'0', 3600, 4200, 2600, 600, '1', 1, 0, 0, 0, 0, '1', 1, b'0')
ON DUPLICATE KEY UPDATE shop_id = VALUES(shop_id), name = VALUES(name), keyword = VALUES(keyword),
  introduction = VALUES(introduction), description = VALUES(description), category_id = VALUES(category_id),
  pic_url = VALUES(pic_url), slider_pic_urls = VALUES(slider_pic_urls), status = VALUES(status), price = VALUES(price),
  market_price = VALUES(market_price), stock = VALUES(stock), delivery_types = VALUES(delivery_types),
  delivery_template_id = VALUES(delivery_template_id), deleted = b'0';

INSERT INTO product_sku
  (id, spu_id, properties, price, market_price, cost_price, bar_code, pic_url, stock, weight, volume,
   first_brokerage_price, second_brokerage_price, sales_count, creator, tenant_id, deleted)
VALUES
  (20001, 20001, '[{"propertyId":0,"propertyName":"规格","valueId":0,"valueName":"50mm / ㎡"}]', 4800, 5600, 3600, 'HT-RM-50', @img, 500, 8, 0.05, 0, 0, 0, '1', 1, b'0'),
  (20002, 20002, '[{"propertyId":0,"propertyName":"规格","valueId":0,"valueName":"A/B 组 200kg"}]', 16800, 18800, 13500, 'JC-PU-200', @img, 300, 200, 0.2, 0, 0, 0, '1', 1, b'0'),
  (20003, 20003, '[{"propertyId":0,"propertyName":"规格","valueId":0,"valueName":"20kg / 桶"}]', 68000, 76000, 51000, 'LD-EP-20', @img, 200, 20, 0.03, 0, 0, 0, '1', 1, b'0'),
  (20004, 20004, '[{"propertyId":0,"propertyName":"规格","valueId":0,"valueName":"20mm / ㎡"}]', 3600, 4200, 2600, 'LD-XS-20', @img, 600, 2, 0.03, 0, 0, 0, '1', 1, b'0')
ON DUPLICATE KEY UPDATE spu_id = VALUES(spu_id), properties = VALUES(properties), price = VALUES(price),
  market_price = VALUES(market_price), cost_price = VALUES(cost_price), pic_url = VALUES(pic_url), stock = VALUES(stock),
  weight = VALUES(weight), volume = VALUES(volume), deleted = b'0';
