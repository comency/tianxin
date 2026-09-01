package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryReservationDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryReservationMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryReturnMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryOperationRetryMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.MALL_INVENTORY_NOT_ENOUGH;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({WmsMallInventoryServiceImpl.class, WmsInventoryOperationRetryServiceImpl.class})
class WmsMallInventoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsMallInventoryService mallInventoryService;
    @Resource
    private WmsInventoryMapper inventoryMapper;
    @Resource
    private WmsInventoryReservationMapper reservationMapper;
    @Resource
    private WmsInventoryReturnMapper returnMapper;
    @Resource
    private WmsInventoryOperationRetryMapper retryMapper;

    @Test
    void reserveAndRelease_areIdempotent() {
        createInventory(1001L, 10L, "10");
        List<WmsMallInventoryService.Item> items = List.of(new WmsMallInventoryService.Item(1001L, 10L, 3));

        assertEquals(10, mallInventoryService.getAvailableStock(1001L, 10L));
        mallInventoryService.reserve("ORDER-LOCK", items);
        mallInventoryService.reserve("ORDER-LOCK", items);
        assertEquals(7, mallInventoryService.getAvailableStock(1001L, 10L));
        assertEquals(1L, reservationMapper.selectCount());

        mallInventoryService.release("ORDER-LOCK", items);
        mallInventoryService.release("ORDER-LOCK", items);
        assertEquals(10, mallInventoryService.getAvailableStock(1001L, 10L));
        assertEquals(WmsInventoryReservationDO.STATUS_RELEASED,
                reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId("ORDER-LOCK", 1001L, 10L).getStatus());
    }

    @Test
    void outboundAndReturn_keepPhysicalInventoryConsistent() {
        createInventory(1002L, 10L, "10");
        List<WmsMallInventoryService.Item> items = List.of(new WmsMallInventoryService.Item(1002L, 10L, 4));

        mallInventoryService.reserve("ORDER-OUT", items);
        mallInventoryService.outbound("ORDER-OUT", items);
        mallInventoryService.outbound("ORDER-OUT", items);
        assertQuantity("6", 1002L, 10L);
        assertEquals(6, mallInventoryService.getAvailableStock(1002L, 10L));

        List<WmsMallInventoryService.Item> partial = List.of(new WmsMallInventoryService.Item(1002L, 10L, 2));
        mallInventoryService.inboundReturn("RETURN-1", "ORDER-OUT", partial);
        mallInventoryService.inboundReturn("RETURN-1", "ORDER-OUT", partial);
        mallInventoryService.inboundReturn("RETURN-2", "ORDER-OUT", partial);
        assertQuantity("10", 1002L, 10L);
        assertEquals(2L, returnMapper.selectCount());
    }

    @Test
    void reserve_rejectsInsufficientAvailableInventory() {
        createInventory(1003L, 10L, "2");
        List<WmsMallInventoryService.Item> items = List.of(new WmsMallInventoryService.Item(1003L, 10L, 3));

        assertServiceException(() -> mallInventoryService.reserve("ORDER-OVER", items), MALL_INVENTORY_NOT_ENOUGH);
        assertEquals(0L, reservationMapper.selectCount());
        assertQuantity("2", 1003L, 10L);
        assertEquals(1L, retryMapper.selectCount());
    }

    @Test
    void getReservationSummaries_returnsBatchFulfillmentStatus() {
        createInventory(1004L, 10L, "10");
        createInventory(1005L, 10L, "10");
        mallInventoryService.reserve("ORDER-LOCKED", List.of(new WmsMallInventoryService.Item(1004L, 10L, 2)));
        mallInventoryService.reserve("ORDER-OUTBOUNDED", List.of(new WmsMallInventoryService.Item(1005L, 10L, 3)));
        mallInventoryService.outbound("ORDER-OUTBOUNDED", List.of(new WmsMallInventoryService.Item(1005L, 10L, 3)));

        Map<String, WmsMallInventoryService.ReservationSummary> summaries = mallInventoryService
                .getReservationSummaries(List.of("ORDER-LOCKED", "ORDER-OUTBOUNDED", "LOCAL-ORDER"));

        assertEquals(2, summaries.size());
        assertEquals("LOCKED", summaries.get("ORDER-LOCKED").status());
        assertEquals(1, summaries.get("ORDER-LOCKED").lockedCount());
        assertEquals("OUTBOUNDED", summaries.get("ORDER-OUTBOUNDED").status());
        assertEquals(1, summaries.get("ORDER-OUTBOUNDED").outboundCount());
    }

    private void createInventory(Long skuId, Long warehouseId, String quantity) {
        inventoryMapper.insert(new WmsInventoryDO().setSkuId(skuId).setWarehouseId(warehouseId)
                .setQuantity(new BigDecimal(quantity)));
    }

    private void assertQuantity(String expected, Long skuId, Long warehouseId) {
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(skuId, warehouseId);
        assertEquals(0, new BigDecimal(expected).compareTo(inventory.getQuantity()));
    }
}
