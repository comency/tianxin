-- 商城店铺/WMS 任务初始化（重复执行安全）
INSERT INTO infra_job
    (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval,
     monitor_timeout, creator, create_time, updater, update_time, deleted)
SELECT 'WMS 库存失败操作重试 Job', 2, 'wmsInventoryOperationRetryJob', '', '0 * * * * ?', 3, 5,
       0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'wmsInventoryOperationRetryJob' AND deleted = b'0');

INSERT INTO infra_job
    (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval,
     monitor_timeout, creator, create_time, updater, update_time, deleted)
SELECT '商城 WMS 库存自动对账 Job', 2, 'productWmsStockReconciliationJob', '', '0 0 2 * * ?', 1, 5,
       0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'productWmsStockReconciliationJob' AND deleted = b'0');
