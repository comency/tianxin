package cn.iocoder.yudao.module.trade.dal.dataobject.settlement;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.trade.enums.settlement.ShopSettlementStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 店铺周期结算单。金额单位均为分，佣金比例单位为万分比。 */
@TableName("trade_shop_settlement")
@KeySequence("trade_shop_settlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopSettlementDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String no;
    private Long shopId;
    private String shopName;
    private LocalDateTime periodStartTime;
    private LocalDateTime periodEndTime;
    private Integer orderCount;
    private Integer orderPayAmount;
    private Integer refundAmount;
    private Integer settlementBaseAmount;
    private Integer commissionRate;
    private Integer platformCommissionAmount;
    private Integer settlementAmount;
    /** {@link ShopSettlementStatusEnum} */
    private Integer status;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private Long settleUserId;
    private LocalDateTime settleTime;
}
