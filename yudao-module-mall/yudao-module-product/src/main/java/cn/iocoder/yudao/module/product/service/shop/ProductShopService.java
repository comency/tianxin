package cn.iocoder.yudao.module.product.service.shop;

import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;

import java.util.List;

/** 产业链店铺服务。 */
public interface ProductShopService {

    Long createShop(ProductShopDO shop);

    void updateShop(ProductShopDO shop);

    void deleteShop(Long id);

    ProductShopDO getShop(Long id);

    ProductShopDO getShopByManagerUserId(Long managerUserId);

    List<ProductShopDO> getShopList();

    void validateEnabledShop(Long id);
}
