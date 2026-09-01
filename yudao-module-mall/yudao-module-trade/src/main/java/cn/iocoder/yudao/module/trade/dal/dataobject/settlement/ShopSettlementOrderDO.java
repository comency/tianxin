package cn.iocoder.yudao.module.trade.dal.dataobject.settlement;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 店铺结算单订单快照，用于防止订单重复结算并保留生成时金额。 */
@TableName("trade_shop_settlement_order")
@KeySequence("trade_shop_settlement_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopSettlementOrderDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long settlementId;
    private Long shopId;
    private Long orderId;
    private String orderNo;
    private LocalDateTime orderFinishTime;
    private Integer payAmount;
    private Integer refundAmount;
    private Integer settlementBaseAmount;
    private Integer platformCommissionAmount;
    private Integer settlementAmount;
    /** true 表示订单正被有效结算单占用；驳回后置空，允许重新生成。 */
    private Boolean active;
}
