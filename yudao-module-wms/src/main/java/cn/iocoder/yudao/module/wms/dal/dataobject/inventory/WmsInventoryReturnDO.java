package cn.iocoder.yudao.module.wms.dal.dataobject.inventory;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/** 商城售后收货确认后生成的 WMS 退货入库幂等记录。 */
@TableName("wms_inventory_return")
@KeySequence("wms_inventory_return_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInventoryReturnDO extends BaseDO {

    @TableId
    private Long id;
    private String returnNo;
    private String orderNo;
    private Long skuId;
    private Long warehouseId;
    private BigDecimal quantity;
}
