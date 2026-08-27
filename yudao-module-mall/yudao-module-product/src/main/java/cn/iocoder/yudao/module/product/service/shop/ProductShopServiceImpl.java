package cn.iocoder.yudao.module.product.service.shop;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;
import cn.iocoder.yudao.module.product.dal.mysql.shop.ProductShopMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

@Service
public class ProductShopServiceImpl implements ProductShopService {

    @Resource
    private ProductShopMapper shopMapper;

    @Override
    public Long createShop(ProductShopDO shop) {
        if (shopMapper.selectByEnterpriseId(shop.getEnterpriseId()) != null) {
            throw exception(SHOP_ENTERPRISE_EXISTS);
        }
        shopMapper.insert(shop);
        // MySQL 的自增主键在当前配置下不会回填到实体，按唯一企业编号回查，保证接口始终返回真实店铺编号。
        return shop.getId() != null ? shop.getId() : shopMapper.selectByEnterpriseId(shop.getEnterpriseId()).getId();
    }

    @Override
    public void updateShop(ProductShopDO shop) {
        ProductShopDO existingShop = validateShopExists(shop.getId());
        if (!Objects.equals(existingShop.getEnterpriseId(), shop.getEnterpriseId())
                && shopMapper.selectByEnterpriseId(shop.getEnterpriseId()) != null) {
            throw exception(SHOP_ENTERPRISE_EXISTS);
        }
        shopMapper.updateById(shop);
    }

    @Override
    public void deleteShop(Long id) {
        validateShopExists(id);
        shopMapper.deleteById(id);
    }

    @Override
    public ProductShopDO getShop(Long id) {
        return shopMapper.selectById(id);
    }

    @Override
    public List<ProductShopDO> getShopList() {
        return shopMapper.selectList();
    }

    @Override
    public void validateEnabledShop(Long id) {
        ProductShopDO shop = validateShopExists(id);
        if (CommonStatusEnum.DISABLE.getStatus().equals(shop.getStatus())) {
            throw exception(SHOP_DISABLED);
        }
    }

    private ProductShopDO validateShopExists(Long id) {
        ProductShopDO shop = shopMapper.selectById(id);
        if (shop == null) {
            throw exception(SHOP_NOT_EXISTS);
        }
        return shop;
    }
}
