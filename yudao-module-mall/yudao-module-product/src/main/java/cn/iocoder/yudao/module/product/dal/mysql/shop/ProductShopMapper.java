package cn.iocoder.yudao.module.product.dal.mysql.shop;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductShopMapper extends BaseMapperX<ProductShopDO> {

    default ProductShopDO selectByEnterpriseId(Long enterpriseId) {
        return selectOne(ProductShopDO::getEnterpriseId, enterpriseId);
    }
}
