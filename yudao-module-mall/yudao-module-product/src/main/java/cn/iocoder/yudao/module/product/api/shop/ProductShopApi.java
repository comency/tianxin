package cn.iocoder.yudao.module.product.api.shop;

import cn.iocoder.yudao.module.product.api.shop.dto.ProductShopRespDTO;

/**
 * 企业店铺 API。
 *
 * @author DAMU
 */
public interface ProductShopApi {

    /**
     * 获得店铺信息。
     *
     * @param id 店铺编号
     * @return 店铺信息，不存在时返回 null
     */
    ProductShopRespDTO getShop(Long id);

    /**
     * 根据后台负责人获得企业店铺。
     *
     * @param managerUserId 后台系统用户编号
     * @return 店铺信息，未绑定时返回 null
     */
    ProductShopRespDTO getShopByManagerUserId(Long managerUserId);

}
