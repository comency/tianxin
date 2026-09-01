package cn.iocoder.yudao.module.wms.dal.mysql.inventory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryReturnDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WmsInventoryReturnMapper extends BaseMapperX<WmsInventoryReturnDO> {

    default WmsInventoryReturnDO selectByReturnNoAndSkuIdAndWarehouseId(String returnNo, Long skuId, Long warehouseId) {
        return selectOne(WmsInventoryReturnDO::getReturnNo, returnNo,
                WmsInventoryReturnDO::getSkuId, skuId,
                WmsInventoryReturnDO::getWarehouseId, warehouseId);
    }

    default BigDecimal selectReturnedQuantity(String orderNo, Long skuId, Long warehouseId) {
        List<Object> values = selectObjs(new QueryWrapper<WmsInventoryReturnDO>()
                .select("COALESCE(SUM(quantity), 0)").eq("order_no", orderNo)
                .eq("sku_id", skuId).eq("warehouse_id", warehouseId));
        return values.isEmpty() ? BigDecimal.ZERO : new BigDecimal(String.valueOf(values.getFirst()));
    }
}
