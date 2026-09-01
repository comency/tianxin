package cn.iocoder.yudao.module.wms.controller.admin.inventory;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryOperationRetryDO;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryOperationRetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/** WMS 库存失败操作的人工补偿入口。 */
@Tag(name = "管理后台 - WMS 库存补偿")
@RestController
@RequestMapping("/wms/inventory-operation-retry")
@Validated
public class WmsInventoryOperationRetryController {

    @Resource
    private WmsInventoryOperationRetryService retryService;

    @GetMapping("/manual")
    @Operation(summary = "获得待人工处理的 WMS 库存操作")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<List<WmsInventoryOperationRetryDO>> getManualList(
            @RequestParam(defaultValue = "50") int limit) {
        return success(retryService.getManualRecords(limit));
    }

    @PostMapping("/retry/{id}")
    @Operation(summary = "人工重试 WMS 库存操作")
    @PreAuthorize("@ss.hasPermission('wms:inventory:update')")
    public CommonResult<Boolean> retry(@PathVariable Long id) {
        return success(retryService.retryById(id));
    }
}
