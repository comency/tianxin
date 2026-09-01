package cn.iocoder.yudao.module.wms.service.inventory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 面向商城的库存适配服务。
 *
 * 当前实现使用本地 WMS 表；商城侧只依赖锁定、释放、出库三个动作，未来可以替换为远程 WMS 调用。
 */
public interface WmsMallInventoryService {

    int getAvailableStock(Long skuId, Long warehouseId);

    /** 获取商城对账所需的 WMS 库存快照。 */
    InventorySnapshot getSnapshot(Long skuId, Long warehouseId);

    /** 批量查询商城订单在 WMS 中的库存履约状态，未映射 WMS 的订单不会出现在结果中。 */
    Map<String, ReservationSummary> getReservationSummaries(Collection<String> orderNos);

    void reserve(String orderNo, List<Item> items);

    void release(String orderNo, List<Item> items);

    void outbound(String orderNo, List<Item> items);

    /** WMS 确认售后退货签收后，实物回补库存。 */
    void inboundReturn(String returnNo, String orderNo, List<Item> items);

    record Item(Long skuId, Long warehouseId, Integer count) {
    }

    record InventorySnapshot(boolean exists, BigDecimal physicalQuantity, BigDecimal lockedQuantity,
                             int availableStock) {
    }

    record ReservationSummary(String status, int totalCount, int lockedCount, int releasedCount,
                              int outboundCount) {
    }
}
