package cn.iocoder.yudao.module.wms.dal.mysql.inventory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryReservationDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WmsInventoryReservationMapper extends BaseMapperX<WmsInventoryReservationDO> {

    default WmsInventoryReservationDO selectByOrderNoAndSkuIdAndWarehouseId(String orderNo, Long skuId, Long warehouseId) {
        return selectOne(WmsInventoryReservationDO::getOrderNo, orderNo,
                WmsInventoryReservationDO::getSkuId, skuId,
                WmsInventoryReservationDO::getWarehouseId, warehouseId);
    }

    default BigDecimal selectLockedQuantity(Long skuId, Long warehouseId) {
        List<Object> values = selectObjs(new QueryWrapper<WmsInventoryReservationDO>()
                        .select("COALESCE(SUM(quantity), 0)")
                        .eq("sku_id", skuId)
                        .eq("warehouse_id", warehouseId)
                        .eq("status", WmsInventoryReservationDO.STATUS_LOCKED));
        return values.isEmpty() ? BigDecimal.ZERO : new BigDecimal(String.valueOf(values.getFirst()));
    }
}
