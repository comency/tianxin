package cn.iocoder.yudao.module.product.service.sku;

import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.service.sku.adapter.ProductWmsInventoryAdapter;
import cn.iocoder.yudao.module.product.service.sku.dto.ProductWmsStockReconciliationDTO;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductSkuWmsReconciliationTest {

    private ProductSkuServiceImpl productSkuService;
    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private ProductSpuService productSpuService;
    @Mock
    private ProductWmsInventoryAdapter productWmsInventoryAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productSkuService = new ProductSkuServiceImpl();
        ReflectionTestUtils.setField(productSkuService, "productSkuMapper", productSkuMapper);
        ReflectionTestUtils.setField(productSkuService, "productSpuService", productSpuService);
        ReflectionTestUtils.setField(productSkuService, "productWmsInventoryAdapter", productWmsInventoryAdapter);
    }

    @Test
    void shouldReportDifferenceAndMissingInventory() {
        ProductSkuDO difference = sku(1L, 100L, 1001L, 10L, 5);
        ProductSkuDO missing = sku(2L, 200L, 2001L, 20L, 2);
        when(productSkuMapper.selectListWithWmsMapping(null)).thenReturn(List.of(difference, missing));
        when(productWmsInventoryAdapter.getSnapshot(1001L, 10L)).thenReturn(
                new ProductWmsInventoryAdapter.InventorySnapshot(true, bd(10), bd(3), 7));
        when(productWmsInventoryAdapter.getSnapshot(2001L, 20L)).thenReturn(
                new ProductWmsInventoryAdapter.InventorySnapshot(false, bd(0), bd(0), 0));
        when(productSpuService.getSpuList(anyCollection())).thenReturn(List.of(
                new ProductSpuDO().setId(100L).setName("岩棉板"),
                new ProductSpuDO().setId(200L).setName("防腐涂料")));

        List<ProductWmsStockReconciliationDTO> result = productSkuService.getWmsStockReconciliation(null);

        assertEquals(2, result.size());
        assertEquals(ProductWmsStockReconciliationDTO.STATUS_CACHE_DIFFERENCE, result.get(0).getStatus());
        assertEquals("岩棉板", result.get(0).getSpuName());
        assertEquals(7, result.get(0).getAvailableStock());
        assertEquals(ProductWmsStockReconciliationDTO.STATUS_MISSING_INVENTORY, result.get(1).getStatus());
    }

    @Test
    void shouldSyncCacheWithoutChangingSales() {
        ProductSkuDO mapped = sku(1L, 100L, 1001L, 10L, 5);
        when(productSkuMapper.selectListWithWmsMapping(null)).thenReturn(List.of(mapped));
        when(productWmsInventoryAdapter.getSnapshot(1001L, 10L)).thenReturn(
                new ProductWmsInventoryAdapter.InventorySnapshot(true, bd(10), bd(3), 7));
        when(productSkuMapper.selectListBySpuId(100L)).thenReturn(List.of(
                new ProductSkuDO().setId(1L).setSpuId(100L).setStock(7),
                new ProductSkuDO().setId(2L).setSpuId(100L).setStock(4)));

        int count = productSkuService.syncWmsStockCache(null);

        assertEquals(1, count);
        verify(productSkuMapper).updateStockCache(1L, 7);
        verify(productSpuService).syncSpuStock(Map.of(100L, 11));
        verify(productSpuService, never()).updateSpuStock(anyMap());
    }

    private static ProductSkuDO sku(Long id, Long spuId, Long wmsSkuId, Long warehouseId, Integer stock) {
        return new ProductSkuDO().setId(id).setSpuId(spuId).setWmsSkuId(wmsSkuId)
                .setWmsWarehouseId(warehouseId).setStock(stock);
    }

    private static BigDecimal bd(int value) {
        return BigDecimal.valueOf(value);
    }

}
