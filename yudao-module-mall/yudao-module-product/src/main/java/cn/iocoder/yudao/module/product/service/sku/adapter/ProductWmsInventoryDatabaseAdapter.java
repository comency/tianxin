package cn.iocoder.yudao.module.product.service.sku.adapter;

import cn.iocoder.yudao.module.wms.service.inventory.WmsMallInventoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/** 当前阶段：商城直接使用本地 WMS 数据库的适配实现。 */
@Component
public class ProductWmsInventoryDatabaseAdapter implements ProductWmsInventoryAdapter {

    @Resource
    private WmsMallInventoryService wmsMallInventoryService;

    @Override
    public int getAvailableStock(Long wmsSkuId, Long warehouseId) {
        return wmsMallInventoryService.getAvailableStock(wmsSkuId, warehouseId);
    }

    @Override
    public void reserve(String orderNo, List<Item> items) {
        wmsMallInventoryService.reserve(orderNo, convert(items));
    }

    @Override
    public void release(String orderNo, List<Item> items) {
        wmsMallInventoryService.release(orderNo, convert(items));
    }

    @Override
    public void outbound(String orderNo, List<Item> items) {
        wmsMallInventoryService.outbound(orderNo, convert(items));
    }

    private static List<WmsMallInventoryService.Item> convert(List<Item> items) {
        return items.stream().map(item -> new WmsMallInventoryService.Item(
                item.wmsSkuId(), item.warehouseId(), item.count())).toList();
    }
}
