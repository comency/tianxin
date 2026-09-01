package cn.iocoder.yudao.module.product.service.sku.adapter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商城现货库存适配接口。
 *
 * <p>当前实现读取同库 WMS 表；未来 WMS 独立后，只替换该接口实现，商品与交易流程无需修改。</p>
 */
public interface ProductWmsInventoryAdapter {

    int getAvailableStock(Long wmsSkuId, Long warehouseId);

    InventorySnapshot getSnapshot(Long wmsSkuId, Long warehouseId);

    void reserve(String orderNo, List<Item> items);

    void release(String orderNo, List<Item> items);

    void outbound(String orderNo, List<Item> items);

    void inboundReturn(String returnNo, String orderNo, List<Item> items);

    record Item(Long wmsSkuId, Long warehouseId, Integer count) {
    }

    record InventorySnapshot(boolean exists, BigDecimal physicalQuantity, BigDecimal lockedQuantity,
                             int availableStock) {
    }
}
