package cn.iocoder.yudao.module.statistics.controller.admin.shop;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.statistics.controller.admin.shop.vo.ShopOperationStatisticsReqVO;
import cn.iocoder.yudao.module.statistics.controller.admin.shop.vo.ShopOperationStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.service.shop.ShopOperationStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 店铺运营统计")
@RestController
@RequestMapping("/statistics/shop")
@Validated
public class ShopOperationStatisticsController {

    @Resource
    private ShopOperationStatisticsService statisticsService;

    @GetMapping("/operation")
    @Operation(summary = "获得店铺运营统计")
    @PreAuthorize("@ss.hasPermission('statistics:shop:query')")
    public CommonResult<List<ShopOperationStatisticsRespVO>> getOperationStatistics(ShopOperationStatisticsReqVO reqVO) {
        if (reqVO.getTimes() == null || reqVO.getTimes().length < 2) {
            return success(statisticsService.getOperationStatistics(reqVO.getShopId(), null, null));
        }
        return success(statisticsService.getOperationStatistics(reqVO.getShopId(), reqVO.getTimes()[0], reqVO.getTimes()[1]));
    }
}
