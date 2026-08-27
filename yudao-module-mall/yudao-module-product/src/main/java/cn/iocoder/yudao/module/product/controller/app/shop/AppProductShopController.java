package cn.iocoder.yudao.module.product.controller.app.shop;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;
import cn.iocoder.yudao.module.product.service.shop.ProductShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SHOP_DISABLED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SHOP_NOT_EXISTS;

@Tag(name = "用户 APP - 企业店铺")
@RestController
@RequestMapping("/product/shop")
public class AppProductShopController {
    @Resource
    private ProductShopService shopService;

    @GetMapping("/list")
    @Operation(summary = "获得启用企业店铺列表")
    @PermitAll
    public CommonResult<List<AppProductShopRespVO>> getShopList() {
        List<ProductShopDO> shops = shopService.getShopList().stream()
                .filter(shop -> CommonStatusEnum.ENABLE.getStatus().equals(shop.getStatus())).toList();
        return success(BeanUtils.toBean(shops, AppProductShopRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得企业店铺详情")
    @PermitAll
    public CommonResult<AppProductShopRespVO> getShop(@RequestParam Long id) {
        ProductShopDO shop = shopService.getShop(id);
        if (shop == null) {
            throw exception(SHOP_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(shop.getStatus())) {
            throw exception(SHOP_DISABLED);
        }
        return success(BeanUtils.toBean(shop, AppProductShopRespVO.class));
    }
}
