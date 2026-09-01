package cn.iocoder.yudao.module.trade.controller.admin.settlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.shop.ProductShopApi;
import cn.iocoder.yudao.module.product.api.shop.dto.ProductShopRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementDO;
import cn.iocoder.yudao.module.trade.service.settlement.ShopSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 店铺结算")
@RestController
@RequestMapping("/trade/shop-settlement")
@Validated
public class ShopSettlementController {

    @Resource
    private ShopSettlementService settlementService;
    @Resource
    private ProductShopApi productShopApi;

    @PostMapping("/generate")
    @Operation(summary = "生成店铺结算单")
    @PreAuthorize("@ss.hasPermission('trade:shop-settlement:create')")
    public CommonResult<Long> generateSettlement(@Valid @RequestBody ShopSettlementGenerateReqVO reqVO) {
        validatePlatformOperator();
        return success(settlementService.generateSettlement(reqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得店铺结算单分页")
    @PreAuthorize("@ss.hasPermission('trade:shop-settlement:query')")
    public CommonResult<PageResult<ShopSettlementRespVO>> getSettlementPage(@Valid ShopSettlementPageReqVO reqVO) {
        applyManagedShopScope(reqVO);
        return success(BeanUtils.toBean(settlementService.getSettlementPage(reqVO), ShopSettlementRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得店铺结算单详情")
    @Parameter(name = "id", required = true, description = "结算单编号")
    @PreAuthorize("@ss.hasPermission('trade:shop-settlement:query')")
    public CommonResult<ShopSettlementRespVO> getSettlement(@RequestParam("id") Long id) {
        ShopSettlementDO settlement = settlementService.getSettlement(id);
        if (settlement == null) {
            return success(null);
        }
        validateManagedSettlement(settlement);
        ShopSettlementRespVO respVO = BeanUtils.toBean(settlement, ShopSettlementRespVO.class);
        respVO.setOrders(BeanUtils.toBean(settlementService.getSettlementOrderList(id),
                ShopSettlementOrderRespVO.class));
        return success(respVO);
    }

    @PutMapping("/audit")
    @Operation(summary = "审核店铺结算单")
    @PreAuthorize("@ss.hasPermission('trade:shop-settlement:audit')")
    public CommonResult<Boolean> auditSettlement(@Valid @RequestBody ShopSettlementAuditReqVO reqVO) {
        validatePlatformOperator();
        settlementService.auditSettlement(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认店铺结算完成")
    @Parameter(name = "id", required = true, description = "结算单编号")
    @PreAuthorize("@ss.hasPermission('trade:shop-settlement:confirm')")
    public CommonResult<Boolean> confirmSettlement(@RequestParam("id") Long id) {
        validatePlatformOperator();
        settlementService.confirmSettlement(id, getLoginUserId());
        return success(true);
    }

    private void applyManagedShopScope(ShopSettlementPageReqVO reqVO) {
        ProductShopRespDTO managedShop = getManagedShop();
        if (managedShop != null) {
            reqVO.setShopId(managedShop.getId());
        }
    }

    private void validateManagedSettlement(ShopSettlementDO settlement) {
        ProductShopRespDTO managedShop = getManagedShop();
        if (managedShop != null && !Objects.equals(managedShop.getId(), settlement.getShopId())) {
            throw exception(FORBIDDEN);
        }
    }

    private void validatePlatformOperator() {
        if (getManagedShop() != null) {
            throw exception(FORBIDDEN);
        }
    }

    private ProductShopRespDTO getManagedShop() {
        return productShopApi.getShopByManagerUserId(getLoginUserId());
    }
}
