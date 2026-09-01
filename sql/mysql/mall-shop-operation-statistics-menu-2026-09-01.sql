-- 店铺运营统计菜单与权限（重复执行安全）
INSERT INTO system_menu
    (name, permission, type, sort, parent_id, path, icon, component, component_name,
     status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '店铺运营统计', 'statistics:shop:query', 2, 20,
       (SELECT parent_id FROM system_menu WHERE permission = 'statistics:product:query' LIMIT 1),
       'shop-operation', 'ep:data-analysis', 'mall/statistics/shop/index', 'MallStatisticsShop',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'statistics:shop:query');
