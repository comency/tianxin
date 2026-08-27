package cn.iocoder.yudao.module.product.service.shop;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.product.dal.dataobject.shop.ProductShopDO;
import cn.iocoder.yudao.module.product.dal.mysql.shop.ProductShopMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SHOP_DISABLED;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SHOP_ENTERPRISE_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductShopServiceImplTest {

    @Mock
    private ProductShopMapper shopMapper;
    @InjectMocks
    private ProductShopServiceImpl shopService;

    @Test
    void createShop_insertsShopWhenEnterpriseHasNoShop() {
        ProductShopDO shop = ProductShopDO.builder().enterpriseId(100L).name("天信材料店").build();
        doAnswer(invocation -> {
            invocation.<ProductShopDO>getArgument(0).setId(1L);
            return 1;
        }).when(shopMapper).insert(any(ProductShopDO.class));

        Long id = shopService.createShop(shop);

        assertEquals(1L, id);
        ArgumentCaptor<ProductShopDO> captor = ArgumentCaptor.forClass(ProductShopDO.class);
        verify(shopMapper).insert(captor.capture());
        assertEquals(100L, captor.getValue().getEnterpriseId());
    }

    @Test
    void createShop_rejectsDuplicateEnterprise() {
        when(shopMapper.selectByEnterpriseId(100L)).thenReturn(ProductShopDO.builder().id(1L).build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> shopService.createShop(ProductShopDO.builder().enterpriseId(100L).build()));

        assertEquals(SHOP_ENTERPRISE_EXISTS.getCode(), exception.getCode());
        verify(shopMapper, never()).insert(any(ProductShopDO.class));
    }

    @Test
    void validateEnabledShop_rejectsDisabledShop() {
        when(shopMapper.selectById(1L)).thenReturn(ProductShopDO.builder()
                .id(1L).status(CommonStatusEnum.DISABLE.getStatus()).build());

        ServiceException exception = assertThrows(ServiceException.class, () -> shopService.validateEnabledShop(1L));

        assertEquals(SHOP_DISABLED.getCode(), exception.getCode());
        verify(shopMapper).selectById(eq(1L));
    }

    @Test
    void updateShop_rejectsAnotherShopEnterprise() {
        when(shopMapper.selectById(1L)).thenReturn(ProductShopDO.builder().id(1L).enterpriseId(100L).build());
        when(shopMapper.selectByEnterpriseId(200L)).thenReturn(ProductShopDO.builder().id(2L).enterpriseId(200L).build());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> shopService.updateShop(ProductShopDO.builder().id(1L).enterpriseId(200L).build()));

        assertEquals(SHOP_ENTERPRISE_EXISTS.getCode(), exception.getCode());
        verify(shopMapper, never()).updateById(any(ProductShopDO.class));
    }
}
