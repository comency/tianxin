package cn.iocoder.yudao.module.product.controller.admin.shop;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.controller.admin.shop.vo.ProductShopRespVO;
import cn.iocoder.yudao.module.product.controller.admin.shop.vo.ProductShopSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;
import cn.iocoder.yudao.module.product.service.shop.ProductShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 产业链企业店铺")
@RestController
@RequestMapping("/product/shop")
@Validated
public class ProductShopController {

    @Resource
    private ProductShopService shopService;

    @PostMapping("/create")
    @Operation(summary = "创建企业店铺")
    @PreAuthorize("@ss.hasPermission('product:shop:create')")
    public CommonResult<Long> createShop(@Valid @RequestBody ProductShopSaveReqVO reqVO) {
        return success(shopService.createShop(BeanUtils.toBean(reqVO, ProductShopDO.class)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新企业店铺")
    @PreAuthorize("@ss.hasPermission('product:shop:update')")
    public CommonResult<Boolean> updateShop(@Valid @RequestBody ProductShopSaveReqVO reqVO) {
        shopService.updateShop(BeanUtils.toBean(reqVO, ProductShopDO.class));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除企业店铺")
    @Parameter(name = "id", description = "店铺编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('product:shop:delete')")
    public CommonResult<Boolean> deleteShop(@RequestParam Long id) {
        shopService.deleteShop(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得企业店铺")
    @Parameter(name = "id", description = "店铺编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('product:shop:query')")
    public CommonResult<ProductShopRespVO> getShop(@RequestParam Long id) {
        return success(BeanUtils.toBean(shopService.getShop(id), ProductShopRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得企业店铺列表")
    @PreAuthorize("@ss.hasPermission('product:shop:query')")
    public CommonResult<List<ProductShopRespVO>> getShopList() {
        return success(BeanUtils.toBean(shopService.getShopList(), ProductShopRespVO.class));
    }

    @GetMapping("/my")
    @Operation(summary = "获得当前后台账号负责的企业店铺")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<ProductShopRespVO> getMyManagedShop() {
        return success(BeanUtils.toBean(shopService.getShopByManagerUserId(getLoginUserId()), ProductShopRespVO.class));
    }
}
