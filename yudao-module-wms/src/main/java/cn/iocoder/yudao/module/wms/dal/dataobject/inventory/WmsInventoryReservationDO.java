package cn.iocoder.yudao.module.wms.dal.dataobject.inventory;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 商城订单的 WMS 库存预占记录。
 *
 * 实物库存仍保存在 {@link WmsInventoryDO}；本表只记录未出库前的锁定数量，
 * 以便后续替换成远程 WMS 适配器时保持相同的业务语义。
 */
@TableName("wms_inventory_reservation")
@TenantIgnore
@KeySequence("wms_inventory_reservation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInventoryReservationDO extends BaseDO {

    public static final int STATUS_LOCKED = 0;
    public static final int STATUS_RELEASED = 1;
    public static final int STATUS_OUTBOUNDED = 2;

    @TableId
    private Long id;
    /** 商城履约订单号，作为幂等键的一部分 */
    private String orderNo;
    private Long skuId;
    private Long warehouseId;
    private BigDecimal quantity;
    private Integer status;
}
