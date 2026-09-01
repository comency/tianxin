package cn.iocoder.yudao.module.wms.job.inventory;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryOperationRetryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** 每分钟处理 WMS 失败操作；达到上限的记录保留给人工补偿。 */
@Component
public class WmsInventoryOperationRetryJob implements JobHandler {

    @Resource
    private WmsInventoryOperationRetryService retryService;

    @Override
    @TenantJob
    public String execute(String param) {
        return "WMS库存失败操作自动重试成功 " + retryService.retryPending(50) + " 条";
    }
}
