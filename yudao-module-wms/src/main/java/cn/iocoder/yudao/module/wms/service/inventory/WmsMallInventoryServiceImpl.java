package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryReservationDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryReturnDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryReservationMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryReturnMapper;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/** 本地数据库 WMS 适配实现。 */
@Service
public class WmsMallInventoryServiceImpl implements WmsMallInventoryService {

    @Resource
    private WmsInventoryMapper inventoryMapper;
    @Resource
    private WmsInventoryReservationMapper reservationMapper;
    @Resource
    private WmsInventoryReturnMapper returnMapper;
    @Resource
    @Lazy
    private WmsInventoryOperationRetryService operationRetryService;

    @Override
    public int getAvailableStock(Long skuId, Long warehouseId) {
        return getSnapshot(skuId, warehouseId).availableStock();
    }

    @Override
    public InventorySnapshot getSnapshot(Long skuId, Long warehouseId) {
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(skuId, warehouseId);
        if (inventory == null) {
            return new InventorySnapshot(false, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }
        BigDecimal locked = reservationMapper.selectLockedQuantity(skuId, warehouseId);
        int availableStock = inventory.getQuantity().subtract(locked).max(BigDecimal.ZERO)
                .setScale(0, RoundingMode.DOWN).intValue();
        return new InventorySnapshot(true, inventory.getQuantity(), locked, availableStock);
    }

    @Override
    public Map<String, ReservationSummary> getReservationSummaries(Collection<String> orderNos) {
        if (orderNos == null || orderNos.isEmpty()) {
            return Map.of();
        }
        Map<String, List<WmsInventoryReservationDO>> reservationMap = reservationMapper.selectListByOrderNos(orderNos)
                .stream().collect(java.util.stream.Collectors.groupingBy(WmsInventoryReservationDO::getOrderNo));
        Map<String, ReservationSummary> result = new HashMap<>();
        reservationMap.forEach((orderNo, reservations) -> {
            int lockedCount = countStatus(reservations, WmsInventoryReservationDO.STATUS_LOCKED);
            int releasedCount = countStatus(reservations, WmsInventoryReservationDO.STATUS_RELEASED);
            int outboundCount = countStatus(reservations, WmsInventoryReservationDO.STATUS_OUTBOUNDED);
            String status = lockedCount == reservations.size() ? "LOCKED"
                    : releasedCount == reservations.size() ? "RELEASED"
                    : outboundCount == reservations.size() ? "OUTBOUNDED" : "MIXED";
            result.put(orderNo, new ReservationSummary(status, reservations.size(), lockedCount, releasedCount,
                    outboundCount));
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(String orderNo, List<Item> items) {
        try {
            for (Item item : items) {
            WmsInventoryReservationDO reservation = reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId(
                    orderNo, item.skuId(), item.warehouseId());
            if (reservation != null) {
                if (!reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_LOCKED)
                        || reservation.getQuantity().compareTo(quantity(item)) != 0) {
                    throw exception(MALL_INVENTORY_OPERATION_INVALID);
                }
                continue;
            }
            WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseIdForUpdate(item.skuId(), item.warehouseId());
            if (inventory == null) {
                throw exception(MALL_INVENTORY_NOT_EXISTS);
            }
            BigDecimal available = inventory.getQuantity().subtract(reservationMapper.selectLockedQuantity(item.skuId(), item.warehouseId()));
            if (available.compareTo(quantity(item)) < 0) {
                throw exception(MALL_INVENTORY_NOT_ENOUGH);
            }
            reservationMapper.insert(new WmsInventoryReservationDO().setOrderNo(orderNo).setSkuId(item.skuId())
                    .setWarehouseId(item.warehouseId()).setQuantity(quantity(item))
                    .setStatus(WmsInventoryReservationDO.STATUS_LOCKED));
            }
        } catch (RuntimeException ex) {
            recordFailure("RESERVE", orderNo, null, items, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(String orderNo, List<Item> items) {
        try {
            for (Item item : items) {
            WmsInventoryReservationDO reservation = reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId(
                    orderNo, item.skuId(), item.warehouseId());
            if (reservation == null || reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_RELEASED)) {
                continue;
            }
            // 已经交给仓库出库的商品，退款并不等于实物已经退回仓；待 WMS 入库确认后再补回库存。
            if (reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_OUTBOUNDED)) {
                continue;
            }
            if (!reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_LOCKED)
                    || reservation.getQuantity().compareTo(quantity(item)) != 0) {
                throw exception(MALL_INVENTORY_OPERATION_INVALID);
            }
            reservationMapper.updateById(new WmsInventoryReservationDO().setId(reservation.getId())
                    .setStatus(WmsInventoryReservationDO.STATUS_RELEASED));
            }
        } catch (RuntimeException ex) {
            recordFailure("RELEASE", orderNo, null, items, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(String orderNo, List<Item> items) {
        try {
            for (Item item : items) {
            WmsInventoryReservationDO reservation = reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId(
                    orderNo, item.skuId(), item.warehouseId());
            if (reservation != null && reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_OUTBOUNDED)) {
                continue;
            }
            if (reservation == null || !reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_LOCKED)
                    || reservation.getQuantity().compareTo(quantity(item)) != 0) {
                throw exception(MALL_INVENTORY_OPERATION_INVALID);
            }
            WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseIdForUpdate(item.skuId(), item.warehouseId());
            if (inventory == null) {
                throw exception(MALL_INVENTORY_NOT_EXISTS);
            }
            if (inventory.getQuantity().compareTo(quantity(item)) < 0) {
                throw exception(MALL_INVENTORY_NOT_ENOUGH);
            }
            inventoryMapper.updateById(new WmsInventoryDO().setId(inventory.getId())
                    .setQuantity(inventory.getQuantity().subtract(quantity(item))));
            reservationMapper.updateById(new WmsInventoryReservationDO().setId(reservation.getId())
                    .setStatus(WmsInventoryReservationDO.STATUS_OUTBOUNDED));
            }
        } catch (RuntimeException ex) {
            recordFailure("OUTBOUND", orderNo, null, items, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundReturn(String returnNo, String orderNo, List<Item> items) {
        try {
            for (Item item : items) {
            BigDecimal returnQuantity = quantity(item);
            WmsInventoryReturnDO existing = returnMapper.selectByReturnNoAndSkuIdAndWarehouseId(
                    returnNo, item.skuId(), item.warehouseId());
            if (existing != null) {
                if (existing.getQuantity().compareTo(returnQuantity) != 0) {
                    throw exception(MALL_INVENTORY_OPERATION_INVALID);
                }
                continue;
            }
            WmsInventoryReservationDO reservation = reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId(
                    orderNo, item.skuId(), item.warehouseId());
            if (reservation == null || !reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_OUTBOUNDED)) {
                throw exception(MALL_INVENTORY_OPERATION_INVALID);
            }
            BigDecimal returned = returnMapper.selectReturnedQuantity(orderNo, item.skuId(), item.warehouseId());
            if (returned.add(returnQuantity).compareTo(reservation.getQuantity()) > 0) {
                throw exception(MALL_INVENTORY_QUANTITY_INVALID);
            }
            WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseIdForUpdate(item.skuId(), item.warehouseId());
            if (inventory == null) {
                throw exception(MALL_INVENTORY_NOT_EXISTS);
            }
            inventoryMapper.updateById(new WmsInventoryDO().setId(inventory.getId())
                    .setQuantity(inventory.getQuantity().add(returnQuantity)));
            returnMapper.insert(new WmsInventoryReturnDO().setReturnNo(returnNo).setOrderNo(orderNo)
                    .setSkuId(item.skuId()).setWarehouseId(item.warehouseId()).setQuantity(returnQuantity));
            }
        } catch (RuntimeException ex) {
            recordFailure("INBOUND_RETURN", orderNo, returnNo, items, ex);
            throw ex;
        }
    }

    private void recordFailure(String operationType, String orderNo, String returnNo, List<Item> items,
                               RuntimeException error) {
        if (WmsInventoryOperationRetryContext.isRetrying()) {
            return;
        }
        try {
            List<WmsInventoryOperationRetryServiceImpl.Payload.Item> payloadItems = items.stream()
                    .map(item -> new WmsInventoryOperationRetryServiceImpl.Payload.Item(item.skuId(), item.warehouseId(), item.count()))
                    .toList();
            operationRetryService.recordFailure(operationType, orderNo, returnNo,
                    JsonUtils.toJsonString(new WmsInventoryOperationRetryServiceImpl.Payload(payloadItems)), error);
        } catch (Exception recordException) {
            // 记录失败不能覆盖原始库存异常，只保留日志供运维排查。
            org.slf4j.LoggerFactory.getLogger(getClass()).error("[recordFailure][记录 WMS 补偿任务失败]", recordException);
        }
    }

    private static BigDecimal quantity(Item item) {
        if (item.count() == null || item.count() <= 0) {
            throw exception(MALL_INVENTORY_QUANTITY_INVALID);
        }
        return BigDecimal.valueOf(item.count());
    }

    private static int countStatus(List<WmsInventoryReservationDO> reservations, int status) {
        return (int) reservations.stream().filter(item -> item.getStatus().equals(status)).count();
    }
}
