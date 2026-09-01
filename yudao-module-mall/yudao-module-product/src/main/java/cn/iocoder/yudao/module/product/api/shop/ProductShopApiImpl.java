package cn.iocoder.yudao.module.product.api.shop;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.shop.dto.ProductShopRespDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;
import cn.iocoder.yudao.module.product.service.shop.ProductShopService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 企业店铺 API 实现。
 *
 * @author DAMU
 */
@Service
@Validated
public class ProductShopApiImpl implements ProductShopApi {

    @Resource
    private ProductShopService shopService;

    @Override
    public ProductShopRespDTO getShop(Long id) {
        ProductShopDO shop = shopService.getShop(id);
        return BeanUtils.toBean(shop, ProductShopRespDTO.class);
    }

    @Override
    public ProductShopRespDTO getShopByManagerUserId(Long managerUserId) {
        ProductShopDO shop = shopService.getShopByManagerUserId(managerUserId);
        return BeanUtils.toBean(shop, ProductShopRespDTO.class);
    }

}
