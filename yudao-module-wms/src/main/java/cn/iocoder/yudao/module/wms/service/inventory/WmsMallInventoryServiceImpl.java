package cn.iocoder.yudao.module.wms.service.inventory;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryReservationDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryReservationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** 本地数据库 WMS 适配实现。 */
@Service
public class WmsMallInventoryServiceImpl implements WmsMallInventoryService {

    @Resource
    private WmsInventoryMapper inventoryMapper;
    @Resource
    private WmsInventoryReservationMapper reservationMapper;

    @Override
    public int getAvailableStock(Long skuId, Long warehouseId) {
        WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseId(skuId, warehouseId);
        if (inventory == null) {
            return 0;
        }
        BigDecimal locked = reservationMapper.selectLockedQuantity(skuId, warehouseId);
        return inventory.getQuantity().subtract(locked).max(BigDecimal.ZERO).setScale(0, RoundingMode.DOWN).intValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(String orderNo, List<Item> items) {
        for (Item item : items) {
            WmsInventoryReservationDO reservation = reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId(
                    orderNo, item.skuId(), item.warehouseId());
            if (reservation != null) {
                Assert.isTrue(reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_LOCKED)
                                && reservation.getQuantity().compareTo(quantity(item)) == 0,
                        "商城订单库存预占状态异常");
                continue;
            }
            WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseIdForUpdate(item.skuId(), item.warehouseId());
            Assert.notNull(inventory, "WMS 库存不存在");
            BigDecimal available = inventory.getQuantity().subtract(reservationMapper.selectLockedQuantity(item.skuId(), item.warehouseId()));
            Assert.isTrue(available.compareTo(quantity(item)) >= 0, "WMS 可售库存不足");
            reservationMapper.insert(new WmsInventoryReservationDO().setOrderNo(orderNo).setSkuId(item.skuId())
                    .setWarehouseId(item.warehouseId()).setQuantity(quantity(item))
                    .setStatus(WmsInventoryReservationDO.STATUS_LOCKED));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(String orderNo, List<Item> items) {
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
            Assert.isTrue(reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_LOCKED), "商城订单库存不能释放");
            Assert.isTrue(reservation.getQuantity().compareTo(quantity(item)) == 0, "商城订单释放数量不一致");
            reservationMapper.updateById(new WmsInventoryReservationDO().setId(reservation.getId())
                    .setStatus(WmsInventoryReservationDO.STATUS_RELEASED));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(String orderNo, List<Item> items) {
        for (Item item : items) {
            WmsInventoryReservationDO reservation = reservationMapper.selectByOrderNoAndSkuIdAndWarehouseId(
                    orderNo, item.skuId(), item.warehouseId());
            if (reservation != null && reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_OUTBOUNDED)) {
                continue;
            }
            Assert.notNull(reservation, "商城订单未锁定 WMS 库存");
            Assert.isTrue(reservation.getStatus().equals(WmsInventoryReservationDO.STATUS_LOCKED), "商城订单库存不能出库");
            Assert.isTrue(reservation.getQuantity().compareTo(quantity(item)) == 0, "商城订单出库数量不一致");
            WmsInventoryDO inventory = inventoryMapper.selectBySkuIdAndWarehouseIdForUpdate(item.skuId(), item.warehouseId());
            Assert.notNull(inventory, "WMS 库存不存在");
            Assert.isTrue(inventory.getQuantity().compareTo(quantity(item)) >= 0, "WMS 实物库存不足");
            inventoryMapper.updateById(new WmsInventoryDO().setId(inventory.getId())
                    .setQuantity(inventory.getQuantity().subtract(quantity(item))));
            reservationMapper.updateById(new WmsInventoryReservationDO().setId(reservation.getId())
                    .setStatus(WmsInventoryReservationDO.STATUS_OUTBOUNDED));
        }
    }

    private static BigDecimal quantity(Item item) {
        Assert.notNull(item.count(), "库存数量不能为空");
        Assert.isTrue(item.count() > 0, "库存数量必须大于 0");
        return BigDecimal.valueOf(item.count());
    }
}
