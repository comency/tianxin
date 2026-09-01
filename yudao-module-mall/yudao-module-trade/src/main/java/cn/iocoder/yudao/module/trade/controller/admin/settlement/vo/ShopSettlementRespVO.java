package cn.iocoder.yudao.module.trade.controller.admin.settlement.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShopSettlementRespVO {

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
    private Integer status;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private Long settleUserId;
    private LocalDateTime settleTime;
    private LocalDateTime createTime;
    private List<ShopSettlementOrderRespVO> orders;
}
